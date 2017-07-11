#!/usr/bin/env python3
import os.path
import sys
import json
import jsonschema
import re

schema_relative_path = ".."
schema_dir = os.path.abspath(schema_relative_path).replace('\\', '/') + "/"
spec_path = os.path.join(schema_dir, "spec.md")
print(schema_dir)

def check_example(schema_path, example):
    full_path = os.path.join(schema_dir, schema_path)
    with open(full_path, 'r') as f:
        try:            
            schema = json.load(f)
        except Exception as e:
            raise Exception("There was an error parsing the JSON schema", e)
    
    resolver = jsonschema.RefResolver(base_uri = 'file:///' + full_path, referrer = schema)
    jsonschema.validate(example, schema, resolver = resolver)

def get_next(iterator):
    try:
        return next(iterator)
    except StopIteration: 
        return None

def get_next_non_blank(iterator):
    line = " "
    while line.isspace():
        line = get_next(iterator)
    return line

def get_example(lines):
    next_line = get_next_non_blank(lines)
    if re.match("#* ?Example$", next_line):
        return get_example_body(lines)
    else:
        return None

def get_example_body(lines):
    line = get_next_non_blank(lines)
    json = ""
    while line and is_preformatted(line):
        json += line[4:]
        line = get_next(lines)
    if json.isspace():
        return None
    else:
        return json

def is_preformatted(line):
    return line[0:4].isspace() and (not line.isspace())

def validate(url, example):
    print("Checking [{}] against {}".format(url, example.strip()))
    try:
        data = json.loads(example)
    except Exception as e:
        raise Exception("There was an error parsing the example JSON", e)
    check_example(url, data)    

def check_spec(spec_path):
    with open(spec_path, 'r') as f:
        spec = f.readlines()
    pattern = re.compile("Schema: \[.+\]\((?P<url>.+)\)$", flags=re.MULTILINE) # \[`(\w|\.)+`\]\((\w|\.+)\)
    lines = iter(spec)
    line = get_next(lines)
    while line:
        match = pattern.match(line)
        if match:
            url = match.group('url')
            example = get_example(lines)
            if example:
                validate(url, example)        
            else:
                raise Exception("No example given for {}".format(url))
        
        line = get_next(lines)

check_spec(spec_path)
print("Finished without errors ☺")
