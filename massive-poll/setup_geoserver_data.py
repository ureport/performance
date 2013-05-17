#!/usr/bin/env python                                                                                                                                        
# vim: ai ts=4 sts=4 et sw=4 encoding=utf-8                                                                                                                  
import ureport_env, os
from celery_exec import execute_async

from ureport.tasks import ping

print "Ok, going to do something...."

from geoserver.models import PollData

pd, _ = PollData.objects.using('geoserver').get_or_create(\
                            district="Fashooda",\
                            poll_id=280,\
                            deployment_id=1)
pd.yes=.75
pd.no=.15
pd.uncategorized=.5
pd.unknown=.5
pd.save()
