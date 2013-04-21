#!/usr/bin/env python
# vim: ai ts=4 sts=4 et sw=4 encoding=utf-8

import os,sys
from django.core.management import setup_environ

import something

sys.path.insert(0,os.getcwd()) 

import perf_settings
 
setup_environ(perf_settings)


print "hello" + str(__name__) + " : " + os.getcwd()

import djcelery
djcelery.setup_loader()

from ureport.tasks import ping

print "Ok, going to do something...."


ping()

print "Ping'd"

print "Going to do it async..."

result = ping.delay()

print "Sent to celery - state of result is " + result.state

while result.ready() != True:
    print "Waiting 0.5 secs for a result..."
    import time
    time.sleep(0.5)

print "Got a result!"
print "Result was [" + str(result.info) + "]"
