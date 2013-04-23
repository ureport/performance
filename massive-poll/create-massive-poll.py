#!/usr/bin/env python
# vim: ai ts=4 sts=4 et sw=4 encoding=utf-8

import ureport_env, os, time, uuid
from celery_exec import execute_async
from splinter import Browser

with Browser() as browser: 
     # Visit URL 
     url = "http://localhost:8088/createpoll/" 
     browser.visit(url) 

     browser.fill("username", "functest")
     browser.fill("password", "functest")
     browser.find_by_css("input[type=submit]").first.click()

     pollName = "PerfTestPoll-" + str(uuid.uuid4())
     print "Created poll called " + pollName                               
     browser.fill("name", pollName)
     browser.fill("question_en", "Will this poll work?")
     browser.select("groups", "54")
     browser.find_link_by_href("javascript:void(0);").click()
     time.sleep(10)



