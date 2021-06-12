import csv
import datetime
import json
import argparse

parser = argparse.ArgumentParser()
parser.add_argument("csv", help="an input csv file")
parser.add_argument("contains_graduate", help="contains subjects for graduate school; \"True\" or \"False\"")

args = parser.parse_args()
date = datetime.datetime.now()
output = {
    "updated": "%d/%02d/%02d" % (date.year, date.month, date.day),
    "subject": []
}

with open(args.csv) as fp:
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

# output
with open("../kdb.json", "w", encoding="utf-8") as fp:
    json.dump(output, fp, indent="\t", ensure_ascii=False)
