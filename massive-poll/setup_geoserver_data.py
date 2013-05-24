#!/usr/bin/env python                                                                                                                                        
# vim: ai ts=4 sts=4 et sw=4 encoding=utf-8                                                                                                                  
import ureport_env, os
from celery_exec import execute_async

from ureport.tasks import ping

print "Ok, going to do something...."

from geoserver.models import PollData, PollCategoryData

pd, _ = PollData.objects.using('geoserver').get_or_create( \
    district="Fashooda", \
    poll_id=245, \
    deployment_id=1)
pd.yes = .75
pd.no = .15
pd.uncategorized = .5
pd.unknown = .5
pd.save()

category_names = ["A", "B", "C"]

values = {
    'A': .2,
    'B': .5,
    'C': .3
}

description = "<br/>".join(["%s: %0.1f%%" % (cat_name, (values[cat_name] * 100)) for cat_name in category_names])

pd, _ = PollCategoryData.objects.using('geoserver').get_or_create(
    district="Fashooda",
    poll_id=241,
    deployment_id=1
)
pd.description = description
pd.top_category = 1
pd.save()

#Setup Uganda data

print "Setting up Uganda data"

pdu, _ = PollData.objects.using('geoserver').get_or_create(district="ABIM", poll_id=245, deployment_id=1)

pdu.yes = .75
pdu.no = .15
pdu.uncategorized = .5
pdu.unknown = .5
pdu.save()

pdu, _ = PollCategoryData.objects.using('geoserver').get_or_create(district="ABIM", poll_id=241, deployment_id=1)
pdu.description = description
pdu.top_category = 1
pdu.save()



