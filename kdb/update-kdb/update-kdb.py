import os
import datetime
import requests
import urllib.parse
import csv
import json
import argparse

parser = argparse.ArgumentParser()
parser.add_argument("contains_graduate", help="contains subjects for graduate school; \"True\" or \"False\"")

args = parser.parse_args()

date = datetime.datetime.now(datetime.timezone(datetime.timedelta(hours=9)))
output = {
    "updated": "%d/%02d/%02d" % (date.year, date.month, date.day),
    "subject": []
}

year = 2021
post = {
    "index": "",
    "locale": "",
    "nendo": year,
    "termCode": "",
    "dayCode": "",
    "periodCode": "",
    "campusCode": "",
    "hierarchy1": "",
    "hierarchy2": "",
    "hierarchy3": "",
    "hierarchy4": "",
    "hierarchy5": "",
    "freeWord": "",
    "_orFlg": 1,
    "_andFlg": 1,
    "_gaiyoFlg": 1,
    "_risyuFlg": 1,
    "_excludeFukaikoFlg": 1,
}

requests.packages.urllib3.util.ssl_.DEFAULT_CIPHERS += "HIGH:!DH:!aNULL"

kdb_url = "https://kdb.tsukuba.ac.jp/"
session = requests.session()
response = session.get(kdb_url)

if response.status_code != 200:
    raise ValueError('System failure on KdB.')

do_url = response.url
qs = urllib.parse.urlparse(do_url).query
query_dict = urllib.parse.parse_qs(qs)

# search
search_post = post.copy()
search_post["_eventId"] = "searchOpeningCourse"
response = session.post(do_url, data=search_post)
do_url = response.url

# download a csv
csv_post = post.copy()
csv_post["_eventId"] = "output"
csv_post["outputFormat"] = 0
response_text = session.post(do_url, data=csv_post).text
if len(response_text) == 0:
    raise ValueError('Response text is empty.')
elif 'sys-err-head' in response_text:
    raise ValueError('System failure on KdB.')

# output the csv file
csv_dir = "../csv"
filename = "%s/kdb-%04d%02d%02d.csv" % (csv_dir, date.year, date.month, date.day)

os.makedirs(csv_dir, exist_ok=True)

with open(filename, "w", encoding="utf-8") as fp:
    fp.write(response_text)

# parse the csv file
with open(filename) as fp:
    reader = csv.reader(fp)

    for line in reader:
        for i in range(13):
            line.pop(4)

        line.pop(2)

        code = line[0]

        # skip the header and empty lines
        if code in ["科目番号", ""]:
            continue

        # subjects for graduate school
        if code[0] == '0' and args.contains_graduate == "False":
            continue

        # delete spaces
        line[2] = line[2].strip()

        output["subject"].append(line)

# output a json file
with open("../kdb.json", "w", encoding="utf-8") as fp:
    json.dump(output, fp, indent="\t", ensure_ascii=False)
