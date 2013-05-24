#!/usr/bin/env python
# vim: ai ts=4 sts=4 et sw=4 encoding=utf-8
import ureport_env, os
from math import ceil
from random import random
from celery_exec import execute_async
from ureport.tasks import ping
from geoserver.models import PollData, PollCategoryData


def generate_random_percentage():
    return ceil(random() * 100)/100

districts = []

file = open('/Users/Nimrod/ThoughtWorks/Code/unicef/ureport/performance/massive-poll/southsudan_districts.txt')
line = file.readline().strip()

while line:
    districts.append(line)
    line = file.readline().strip()
file.close()

print('Top 5 districts:')
for index in range(5):
    print(districts[index])

for district in districts:
    pd, _ = PollData.objects.using('geoserver').get_or_create(district=district, poll_id=245, deployment_id=1)
    yes_percent = generate_random_percentage()
    no_percent = 1 - yes_percent

    pd.yes = yes_percent
    pd.no = no_percent
    pd.uncategorized = 0.0
    pd.unknown = 0.0
    pd.save()





