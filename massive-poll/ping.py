#!/usr/bin/env python
# vim: ai ts=4 sts=4 et sw=4 encoding=utf-8


import ureport_env, os
from celery_exec import execute_async

from ureport.tasks import ping

print "Ok, going to do something...."


ping()

print "Ping'd"


execute_async(ping)
