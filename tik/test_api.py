#!/usr/bin/env python3
"""Mobile REST exam: standalone sequential HTTP checks + compact summary.
Python 3.10+, no third-party packages. Do not run with python -O.
Run: python test_api.py --base-url http://localhost:8080
HTTP API only. Does not connect to a database or run SQL.
"""
import argparse, inspect, time, sys, traceback
from collections import defaultdict
TEST_CASES = []
TIMEOUT = 10
TRACKED_IDS = set()
ATTEMPTED_SKUS = set()
BASELINE_IDS = set()
LAST_REQUEST = None

def cases(fields, values):

    def decorate(fn):
        fn._case_fields = fields.split(',')
        fn._case_values = values
        return fn
    return decorate
EXPECTED_PRODUCTS = [{'id': 1, 'sku': 'MOB-001', 'name': 'Apple Demo 01', 'brand': {'id': 1, 'name': 'Apple'}, 'price': 0.01, 'stock': 0, 'ramGb': 2, 'storageGb': 32, 'color': 'Black', 'description': 'โทรศัพท์ตัวอย่างลำดับ 1', 'active': True}, {'id': 2, 'sku': 'MOB-002', 'name': 'Samsung Demo 02', 'brand': {'id': 2, 'name': 'Samsung'}, 'price': 99999999.99, 'stock': 100000, 'ramGb': 6, 'storageGb': 64, 'color': 'White', 'description': 'โทรศัพท์ตัวอย่างลำดับ 2', 'active': True}, {'id': 3, 'sku': 'MOB-003', 'name': 'Xiaomi Demo 03', 'brand': {'id': 3, 'name': 'Xiaomi'}, 'price': 8490.0, 'stock': 21, 'ramGb': 8, 'storageGb': 128, 'color': 'Blue', 'description': 'โทรศัพท์ตัวอย่างลำดับ 3', 'active': True}, {'id': 4, 'sku': 'MOB-004', 'name': 'OPPO Demo 04', 'brand': {'id': 4, 'name': 'OPPO'}, 'price': 9990.25, 'stock': 28, 'ramGb': 12, 'storageGb': 256, 'color': 'Green', 'description': 'โทรศัพท์ตัวอย่างลำดับ 4', 'active': False}, {'id': 5, 'sku': 'MOB-005', 'name': 'vivo Demo 05', 'brand': {'id': 5, 'name': 'vivo'}, 'price': 11490.5, 'stock': 35, 'ramGb': 16, 'storageGb': 512, 'color': 'สีทอง', 'description': None, 'active': True}, {'id': 6, 'sku': 'MOB-006', 'name': 'Google Demo 06', 'brand': {'id': 6, 'name': 'Google'}, 'price': 12990.0, 'stock': 1, 'ramGb': 24, 'storageGb': 1024, 'color': 'Silver', 'description': 'โทรศัพท์ตัวอย่างลำดับ 6', 'active': True}, {'id': 7, 'sku': 'MOB-007', 'name': 'Nothing Demo 07', 'brand': {'id': 7, 'name': 'Nothing'}, 'price': 14490.25, 'stock': 8, 'ramGb': 4, 'storageGb': 32, 'color': 'Black', 'description': '', 'active': True}, {'id': 8, 'sku': 'MOB-008', 'name': 'Apple Demo 08', 'brand': {'id': 1, 'name': 'Apple'}, 'price': 15990.5, 'stock': 15, 'ramGb': 6, 'storageGb': 64, 'color': 'White', 'description': 'โทรศัพท์ตัวอย่างลำดับ 8', 'active': False}, {'id': 9, 'sku': 'MOB-009', 'name': 'Samsung Demo 09', 'brand': {'id': 2, 'name': 'Samsung'}, 'price': 17490.0, 'stock': 22, 'ramGb': 8, 'storageGb': 128, 'color': 'Blue', 'description': 'โทรศัพท์ตัวอย่างลำดับ 9', 'active': True}, {'id': 10, 'sku': 'MOB-010', 'name': 'Xiaomi Demo 10', 'brand': {'id': 3, 'name': 'Xiaomi'}, 'price': 18990.25, 'stock': 29, 'ramGb': 12, 'storageGb': 256, 'color': 'Green', 'description': None, 'active': True}, {'id': 11, 'sku': 'MOB-011', 'name': 'OPPO Demo 11', 'brand': {'id': 4, 'name': 'OPPO'}, 'price': 20490.5, 'stock': 36, 'ramGb': 16, 'storageGb': 512, 'color': 'สีทอง', 'description': 'โทรศัพท์ตัวอย่างลำดับ 11', 'active': True}, {'id': 12, 'sku': 'MOB-012', 'name': 'vivo Demo 12', 'brand': {'id': 5, 'name': 'vivo'}, 'price': 3990.0, 'stock': 2, 'ramGb': 24, 'storageGb': 1024, 'color': 'Silver', 'description': 'โทรศัพท์ตัวอย่างลำดับ 12', 'active': False}, {'id': 13, 'sku': 'MOB-013', 'name': 'Google Demo 13', 'brand': {'id': 6, 'name': 'Google'}, 'price': 5490.25, 'stock': 9, 'ramGb': 4, 'storageGb': 32, 'color': 'Black', 'description': 'โทรศัพท์ตัวอย่างลำดับ 13', 'active': True}, {'id': 14, 'sku': 'MOB-014', 'name': 'Nothing Demo 14', 'brand': {'id': 7, 'name': 'Nothing'}, 'price': 6990.5, 'stock': 16, 'ramGb': 6, 'storageGb': 64, 'color': 'White', 'description': '', 'active': True}, {'id': 15, 'sku': 'MOB-015', 'name': 'Apple Demo 15', 'brand': {'id': 1, 'name': 'Apple'}, 'price': 8490.0, 'stock': 23, 'ramGb': 8, 'storageGb': 128, 'color': 'Blue', 'description': None, 'active': True}, {'id': 16, 'sku': 'MOB-016', 'name': 'Samsung Demo 16', 'brand': {'id': 2, 'name': 'Samsung'}, 'price': 9990.25, 'stock': 30, 'ramGb': 12, 'storageGb': 256, 'color': 'Green', 'description': 'โทรศัพท์ตัวอย่างลำดับ 16', 'active': False}, {'id': 17, 'sku': 'MOB-017', 'name': 'Xiaomi Demo 17', 'brand': {'id': 3, 'name': 'Xiaomi'}, 'price': 11490.5, 'stock': 37, 'ramGb': 16, 'storageGb': 512, 'color': 'สีทอง', 'description': 'โทรศัพท์ตัวอย่างลำดับ 17', 'active': True}, {'id': 18, 'sku': 'MOB-018', 'name': 'OPPO Demo 18', 'brand': {'id': 4, 'name': 'OPPO'}, 'price': 12990.0, 'stock': 3, 'ramGb': 24, 'storageGb': 1024, 'color': 'Silver', 'description': 'โทรศัพท์ตัวอย่างลำดับ 18', 'active': True}, {'id': 19, 'sku': 'MOB-019', 'name': 'vivo Demo 19', 'brand': {'id': 5, 'name': 'vivo'}, 'price': 14490.25, 'stock': 10, 'ramGb': 4, 'storageGb': 32, 'color': 'Black', 'description': 'โทรศัพท์ตัวอย่างลำดับ 19', 'active': True}, {'id': 20, 'sku': 'MOB-020', 'name': 'Google Demo 20', 'brand': {'id': 6, 'name': 'Google'}, 'price': 15990.5, 'stock': 17, 'ramGb': 6, 'storageGb': 64, 'color': 'White', 'description': None, 'active': False}, {'id': 21, 'sku': 'MOB-021', 'name': 'Nothing Demo 21', 'brand': {'id': 7, 'name': 'Nothing'}, 'price': 17490.0, 'stock': 24, 'ramGb': 8, 'storageGb': 128, 'color': 'Blue', 'description': '', 'active': True}, {'id': 22, 'sku': 'MOB-022', 'name': 'Apple Demo 22', 'brand': {'id': 1, 'name': 'Apple'}, 'price': 18990.25, 'stock': 31, 'ramGb': 12, 'storageGb': 256, 'color': 'Green', 'description': 'โทรศัพท์ตัวอย่างลำดับ 22', 'active': True}, {'id': 23, 'sku': 'MOB-023', 'name': 'Samsung Demo 23', 'brand': {'id': 2, 'name': 'Samsung'}, 'price': 20490.5, 'stock': 38, 'ramGb': 16, 'storageGb': 512, 'color': 'สีทอง', 'description': 'โทรศัพท์ตัวอย่างลำดับ 23', 'active': True}, {'id': 24, 'sku': 'MOB-024', 'name': 'Xiaomi Demo 24', 'brand': {'id': 3, 'name': 'Xiaomi'}, 'price': 3990.0, 'stock': 4, 'ramGb': 24, 'storageGb': 1024, 'color': 'Silver', 'description': 'โทรศัพท์ตัวอย่างลำดับ 24', 'active': False}, {'id': 25, 'sku': 'MOB-025', 'name': 'OPPO Demo 25', 'brand': {'id': 4, 'name': 'OPPO'}, 'price': 5490.25, 'stock': 11, 'ramGb': 4, 'storageGb': 32, 'color': 'Black', 'description': None, 'active': True}, {'id': 26, 'sku': 'MOB-026', 'name': 'vivo Demo 26', 'brand': {'id': 5, 'name': 'vivo'}, 'price': 6990.5, 'stock': 18, 'ramGb': 6, 'storageGb': 64, 'color': 'White', 'description': 'โทรศัพท์ตัวอย่างลำดับ 26', 'active': True}, {'id': 27, 'sku': 'MOB-027', 'name': 'Google Demo 27', 'brand': {'id': 6, 'name': 'Google'}, 'price': 8490.0, 'stock': 25, 'ramGb': 8, 'storageGb': 128, 'color': 'Blue', 'description': 'โทรศัพท์ตัวอย่างลำดับ 27', 'active': True}, {'id': 28, 'sku': 'MOB-028', 'name': 'Nothing Demo 28', 'brand': {'id': 7, 'name': 'Nothing'}, 'price': 9990.25, 'stock': 32, 'ramGb': 12, 'storageGb': 256, 'color': 'Green', 'description': '', 'active': False}, {'id': 29, 'sku': 'MOB-029', 'name': 'Apple Demo 29', 'brand': {'id': 1, 'name': 'Apple'}, 'price': 11490.5, 'stock': 39, 'ramGb': 16, 'storageGb': 512, 'color': 'สีทอง', 'description': 'โทรศัพท์ตัวอย่างลำดับ 29', 'active': True}, {'id': 30, 'sku': 'MOB-030', 'name': 'Samsung Demo 30', 'brand': {'id': 2, 'name': 'Samsung'}, 'price': 12990.0, 'stock': 5, 'ramGb': 24, 'storageGb': 1024, 'color': 'Silver', 'description': None, 'active': True}, {'id': 31, 'sku': 'MOB-031', 'name': 'Xiaomi Demo 31', 'brand': {'id': 3, 'name': 'Xiaomi'}, 'price': 14490.25, 'stock': 12, 'ramGb': 4, 'storageGb': 32, 'color': 'Black', 'description': 'โทรศัพท์ตัวอย่างลำดับ 31', 'active': True}, {'id': 32, 'sku': 'MOB-032', 'name': 'OPPO Demo 32', 'brand': {'id': 4, 'name': 'OPPO'}, 'price': 15990.5, 'stock': 19, 'ramGb': 6, 'storageGb': 64, 'color': 'White', 'description': 'โทรศัพท์ตัวอย่างลำดับ 32', 'active': False}, {'id': 33, 'sku': 'MOB-033', 'name': 'vivo Demo 33', 'brand': {'id': 5, 'name': 'vivo'}, 'price': 17490.0, 'stock': 26, 'ramGb': 8, 'storageGb': 128, 'color': 'Blue', 'description': 'โทรศัพท์ตัวอย่างลำดับ 33', 'active': True}, {'id': 34, 'sku': 'MOB-034', 'name': 'Google Demo 34', 'brand': {'id': 6, 'name': 'Google'}, 'price': 18990.25, 'stock': 33, 'ramGb': 12, 'storageGb': 256, 'color': 'Green', 'description': 'โทรศัพท์ตัวอย่างลำดับ 34', 'active': True}, {'id': 35, 'sku': 'MOB-035', 'name': 'Nothing Demo 35', 'brand': {'id': 7, 'name': 'Nothing'}, 'price': 20490.5, 'stock': 40, 'ramGb': 16, 'storageGb': 512, 'color': 'สีทอง', 'description': None, 'active': True}, {'id': 36, 'sku': 'MOB-036', 'name': 'Apple Demo 36', 'brand': {'id': 1, 'name': 'Apple'}, 'price': 3990.0, 'stock': 6, 'ramGb': 24, 'storageGb': 1024, 'color': 'Silver', 'description': 'โทรศัพท์ตัวอย่างลำดับ 36', 'active': False}, {'id': 37, 'sku': 'MOB-037', 'name': 'Samsung Demo 37', 'brand': {'id': 2, 'name': 'Samsung'}, 'price': 5490.25, 'stock': 13, 'ramGb': 4, 'storageGb': 32, 'color': 'Black', 'description': 'โทรศัพท์ตัวอย่างลำดับ 37', 'active': True}, {'id': 38, 'sku': 'MOB-038', 'name': 'Xiaomi Demo 38', 'brand': {'id': 3, 'name': 'Xiaomi'}, 'price': 6990.5, 'stock': 20, 'ramGb': 6, 'storageGb': 64, 'color': 'White', 'description': 'โทรศัพท์ตัวอย่างลำดับ 38', 'active': True}, {'id': 39, 'sku': 'MOB-039', 'name': 'OPPO Demo 39', 'brand': {'id': 4, 'name': 'OPPO'}, 'price': 8490.0, 'stock': 27, 'ramGb': 8, 'storageGb': 128, 'color': 'Blue', 'description': 'โทรศัพท์ตัวอย่างลำดับ 39', 'active': True}, {'id': 40, 'sku': 'MOB-040', 'name': 'vivo Demo 40', 'brand': {'id': 5, 'name': 'vivo'}, 'price': 9990.25, 'stock': 34, 'ramGb': 12, 'storageGb': 256, 'color': 'Green', 'description': None, 'active': False}, {'id': 41, 'sku': 'MOB-041', 'name': 'Google Demo 41', 'brand': {'id': 6, 'name': 'Google'}, 'price': 11490.5, 'stock': 0, 'ramGb': 16, 'storageGb': 512, 'color': 'สีทอง', 'description': 'โทรศัพท์ตัวอย่างลำดับ 41', 'active': True}, {'id': 42, 'sku': 'MOB-042', 'name': 'Nothing Demo 42', 'brand': {'id': 7, 'name': 'Nothing'}, 'price': 12990.0, 'stock': 7, 'ramGb': 24, 'storageGb': 1024, 'color': 'Silver', 'description': '', 'active': True}, {'id': 43, 'sku': 'MOB-043', 'name': 'Apple Demo 43', 'brand': {'id': 1, 'name': 'Apple'}, 'price': 14490.25, 'stock': 14, 'ramGb': 4, 'storageGb': 32, 'color': 'Black', 'description': 'โทรศัพท์ตัวอย่างลำดับ 43', 'active': True}, {'id': 44, 'sku': 'MOB-044', 'name': 'Samsung Demo 44', 'brand': {'id': 2, 'name': 'Samsung'}, 'price': 15990.5, 'stock': 21, 'ramGb': 6, 'storageGb': 64, 'color': 'White', 'description': 'โทรศัพท์ตัวอย่างลำดับ 44', 'active': False}, {'id': 45, 'sku': 'MOB-045', 'name': 'Xiaomi Demo 45', 'brand': {'id': 3, 'name': 'Xiaomi'}, 'price': 17490.0, 'stock': 28, 'ramGb': 8, 'storageGb': 128, 'color': 'Blue', 'description': None, 'active': True}, {'id': 46, 'sku': 'MOB-046', 'name': 'OPPO Demo 46', 'brand': {'id': 4, 'name': 'OPPO'}, 'price': 18990.25, 'stock': 35, 'ramGb': 12, 'storageGb': 256, 'color': 'Green', 'description': 'โทรศัพท์ตัวอย่างลำดับ 46', 'active': True}, {'id': 47, 'sku': 'MOB-047', 'name': 'vivo Demo 47', 'brand': {'id': 5, 'name': 'vivo'}, 'price': 20490.5, 'stock': 1, 'ramGb': 16, 'storageGb': 512, 'color': 'สีทอง', 'description': 'โทรศัพท์ตัวอย่างลำดับ 47', 'active': True}, {'id': 48, 'sku': 'MOB-048', 'name': 'Google Demo 48', 'brand': {'id': 6, 'name': 'Google'}, 'price': 3990.0, 'stock': 8, 'ramGb': 24, 'storageGb': 1024, 'color': 'Silver', 'description': 'โทรศัพท์ตัวอย่างลำดับ 48', 'active': False}]
EXPECTED_BRANDS = [{'id': 1, 'name': 'Apple', 'country': 'USA'}, {'id': 2, 'name': 'Samsung', 'country': 'South Korea'}, {'id': 3, 'name': 'Xiaomi', 'country': 'China'}, {'id': 4, 'name': 'OPPO', 'country': 'China'}, {'id': 5, 'name': 'vivo', 'country': 'China'}, {'id': 6, 'name': 'Google', 'country': 'USA'}, {'id': 7, 'name': 'Nothing', 'country': 'United Kingdom'}, {'id': 8, 'name': 'ExamBrand', 'country': 'Thailand'}]
import json, os, uuid
from pathlib import Path
from urllib.request import Request, urlopen
from urllib.error import HTTPError
from urllib.parse import urlsplit
BASE = os.environ.get('BASE_URL', 'http://localhost:8080').rstrip('/')
PREFIX = '/api/v1'
PRODUCT_KEYS = {'id', 'sku', 'name', 'brand', 'price', 'stock', 'ramGb', 'storageGb', 'color', 'description', 'active'}

def _http(method, path, body=None, headers=None, raw=None):
    h = {'Accept': 'application/json'}
    if body is not None or raw is not None:
        h['Content-Type'] = 'application/json'
    h.update(headers or {})
    data = raw if raw is not None else json.dumps(body, ensure_ascii=False).encode() if body is not None else None
    req = Request(BASE + PREFIX + path, data=data, headers=h, method=method)
    try:
        r = urlopen(req, timeout=TIMEOUT)
    except HTTPError as e:
        r = e
    with r:
        payload = r.read()
        parsed = None
        if payload:
            try:
                parsed = json.loads(payload)
            except (ValueError, UnicodeDecodeError):
                parsed = {'non_json_response': payload.decode('utf-8', errors='replace')[:200]}
        return (r.status, r.headers, parsed, payload)

def ok(r, status):
    mark_expected(r, status)
    _actual_1 = r[0]
    _expected_1 = status
    _require(_actual_1 == _expected_1, 'r[0] == status', _expected_1, _actual_1)
    if status != 204:
        _actual_2 = r[1].get_content_type()
        _expected_2 = 'application/json'
        _require(_actual_2 == _expected_2, "r[1].get_content_type() == 'application/json'", _expected_2, _actual_2)
    return r[2]

def error(r, status, code, path, fields=None):
    b = ok(r, status)
    _actual_3 = set(b)
    _expected_3 = {'status', 'code', 'message', 'path', 'fieldErrors'}
    _require(_actual_3 == _expected_3, "set(b) == {'status', 'code', 'message', 'path', 'fieldErrors'}", _expected_3, _actual_3)
    _actual_4 = b['status']
    _expected_4 = status
    _require(_actual_4 == _expected_4, "b['status'] == status", _expected_4, _actual_4)
    _actual_5 = b['code']
    _expected_5 = code
    _require(_actual_5 == _expected_5, "b['code'] == code", _expected_5, _actual_5)
    _actual_6 = b['path']
    _expected_6 = PREFIX + path.split('?')[0]
    _require(_actual_6 == _expected_6, "b['path'] == PREFIX + path.split('?')[0]", _expected_6, _actual_6)
    _require(isinstance(b['message'], str) and b['message'].strip(), "isinstance(b['message'], str) and b['message'].strip()", True, 'condition is false')
    _require(isinstance(b['fieldErrors'], list), "isinstance(b['fieldErrors'], list)", True, 'condition is false')
    for e in b['fieldErrors']:
        _require(set(e) == {'field', 'message'} and isinstance(e['field'], str), "set(e) == {'field', 'message'} and isinstance(e['field'], str)", True, 'condition is false')
        _require(isinstance(e['message'], str) and e['message'].strip(), "isinstance(e['message'], str) and e['message'].strip()", True, 'condition is false')
    if fields is None:
        _actual_11 = b['fieldErrors']
        _expected_11 = []
        _require(_actual_11 == _expected_11, "b['fieldErrors'] == []", _expected_11, _actual_11)
    else:
        _actual_12 = set(fields)
        _expected_12 = set((e['field'] for e in b['fieldErrors']))
        _require(_actual_12 <= _expected_12, "set(fields) <= set((e['field'] for e in b['fieldErrors']))", _expected_12, _actual_12)

def dto(b):
    _actual_13 = set(b)
    _expected_13 = PRODUCT_KEYS
    _require(_actual_13 == _expected_13, 'set(b) == PRODUCT_KEYS', _expected_13, _actual_13)
    _actual_14 = set(b['brand'])
    _expected_14 = {'id', 'name'}
    _require(_actual_14 == _expected_14, "set(b['brand']) == {'id', 'name'}", _expected_14, _actual_14)
    _require(isinstance(b['id'], int) and (not isinstance(b['id'], bool)), "isinstance(b['id'], int) and (not isinstance(b['id'], bool))", True, 'condition is false')
    _require(isinstance(b['price'], (int, float)) and (not isinstance(b['price'], bool)), "isinstance(b['price'], (int, float)) and (not isinstance(b['price'], bool))", True, 'condition is false')
    _require(isinstance(b['active'], bool), "isinstance(b['active'], bool)", True, 'condition is false')

def payload():
    return dict(sku='TST-' + uuid.uuid4().hex[:16].upper(), name='Exam Phone', brandId=1, price=15990.5, stock=10, ramGb=8, storageGb=256, color='Black', description='เครื่องทดสอบ', active=True)

def test_seed():
    brands = ok(call('GET', '/brands'), 200)
    _actual_18 = brands
    _expected_18 = EXPECTED_BRANDS
    _require(_actual_18 == _expected_18, 'brands == EXPECTED_BRANDS', _expected_18, _actual_18)
    ps = ok(call('GET', '/products'), 200)
    _actual_19 = ps
    _expected_19 = EXPECTED_PRODUCTS
    _require(_actual_19 == _expected_19, 'ps == EXPECTED_PRODUCTS', _expected_19, _actual_19)
    for p in ps:
        dto(p)
    _actual_20 = ok(call('GET', '/products/1'), 200)
    _expected_20 = ps[0]
    _require(_actual_20 == _expected_20, "ok(call('GET', '/products/1'), 200) == ps[0]", _expected_20, _actual_20)

@cases('brand', [1, 7, 8])
def test_filter(brand):
    expected = EXPECTED_PRODUCTS
    _actual_21 = ok(call('GET', f'/products?brandId={brand}'), 200)
    _expected_21 = [p for p in expected if p['brand']['id'] == brand]
    _require(_actual_21 == _expected_21, "ok(call('GET', f'/products?brandId={brand}'), 200) == [p for p in expected if p['brand']['id'] == brand]", _expected_21, _actual_21)

def test_create_location(created):
    p, b, r = created
    dto(b)
    _actual_22 = urlsplit(r[1]['Location']).path
    _expected_22 = PREFIX + f"/products/{b['id']}"
    _require(_actual_22 == _expected_22, 'urlsplit(r[1][\'Location\']).path == PREFIX + f"/products/{b[\'id\']}"', _expected_22, _actual_22)
    _actual_23 = ok(call('GET', f"/products/{b['id']}"), 200)
    _expected_23 = b
    _require(_actual_23 == _expected_23, 'ok(call(\'GET\', f"/products/{b[\'id\']}"), 200) == b', _expected_23, _actual_23)
    _require(b['name'] == p['name'] and b['price'] == p['price'] and (b['brand'] == {'id': 1, 'name': 'Apple'}), "b['name'] == p['name'] and b['price'] == p['price'] and (b['brand'] == {'id': 1, 'name': 'Apple'})", True, 'condition is false')

def test_full_update_and_idempotence(created):
    p, b, _ = created
    path = f"/products/{b['id']}"
    p.update(name='Updated', brandId=2, price=0.01, stock=0, ramGb=2, storageGb=32, color='สีทอง', active=False)
    p.pop('description')
    updated = ok(call('PUT', path, p), 200)
    dto(updated)
    _require(updated['id'] == b['id'] and updated['description'] is None, "updated['id'] == b['id'] and updated['description'] is None", True, 'condition is false')
    expected = {k: v for k, v in p.items() if k != 'brandId'}
    _require(all((updated[k] == v for k, v in expected.items())), 'all((updated[k] == v for k, v in expected.items()))', True, 'condition is false')
    _actual_27 = updated['brand']
    _expected_27 = {'id': 2, 'name': 'Samsung'}
    _require(_actual_27 == _expected_27, "updated['brand'] == {'id': 2, 'name': 'Samsung'}", _expected_27, _actual_27)
    count = len(ok(call('GET', '/products'), 200))
    _actual_28 = ok(call('PUT', path, p), 200)
    _expected_28 = updated
    _require(_actual_28 == _expected_28, "ok(call('PUT', path, p), 200) == updated", _expected_28, _actual_28)
    _actual_29 = ok(call('GET', path), 200)
    _expected_29 = updated
    _require(_actual_29 == _expected_29, "ok(call('GET', path), 200) == updated", _expected_29, _actual_29)
    _actual_30 = len(ok(call('GET', '/products'), 200))
    _expected_30 = count
    _require(_actual_30 == _expected_30, "len(ok(call('GET', '/products'), 200)) == count", _expected_30, _actual_30)

def test_delete(created):
    _, b, _ = created
    path = f"/products/{b['id']}"
    r = call('DELETE', path)
    _actual_31 = r[0]
    _expected_31 = 204
    _require(_actual_31 == _expected_31, 'r[0] == 204', _expected_31, _actual_31)
    _actual_32 = r[3]
    _expected_32 = b''
    _require(_actual_32 == _expected_32, "r[3] == b''", _expected_32, _actual_32)
    error(call('GET', path), 404, 'PRODUCT_NOT_FOUND', path)
    error(call('DELETE', path), 404, 'PRODUCT_NOT_FOUND', path)

@cases('method', ['GET', 'PUT', 'DELETE'])
def test_missing_product(method):
    path = '/products/999999'
    error(call(method, path, payload() if method == 'PUT' else None), 404, 'PRODUCT_NOT_FOUND', path)

@cases('path', ['/products/0', '/products/-1', '/products/abc', '/products/1.0', '/products/9223372036854775808', '/products?brandId=', '/products?brandId=0', '/products?brandId=x', '/products?brandId=1&brandId=2', '/products?sort=name'])
def test_invalid_path_query(path):
    error(call('GET', path), 400, 'INVALID_REQUEST', path)

def test_missing_brand_filter():
    error(call('GET', '/products?brandId=999999'), 404, 'BRAND_NOT_FOUND', '/products')

@cases('field,value', [('sku', 'ab'), ('sku', ''), ('name', '   '), ('name', 'x' * 101), ('price', 0), ('price', 100000000), ('price', 1.001), ('stock', -1), ('stock', 100001), ('ramGb', 3), ('storageGb', 100), ('color', ''), ('description', 'x' * 501), ('brandId', 0), ('active', None)])
def test_validation(field, value):
    p = payload()
    p[field] = value
    before = ok(call('GET', '/products'), 200)
    error(call('POST', '/products', p), 400, 'VALIDATION_ERROR', '/products', [field])
    _actual_33 = ok(call('GET', '/products'), 200)
    _expected_33 = before
    _require(_actual_33 == _expected_33, "ok(call('GET', '/products'), 200) == before", _expected_33, _actual_33)

@cases('field', ['sku', 'name', 'brandId', 'price', 'stock', 'ramGb', 'storageGb', 'color', 'active'])
def test_missing_required(field):
    p = payload()
    p.pop(field)
    error(call('POST', '/products', p), 400, 'VALIDATION_ERROR', '/products', [field])

@cases('field,value', [('price', '100'), ('stock', 1.5), ('stock', 1.0), ('active', 'true'), ('active', 1), ('name', 123), ('brandId', '1'), ('id', 100), ('unknown', True)])
def test_invalid_types(field, value):
    p = payload()
    p[field] = value
    error(call('POST', '/products', p), 400, 'INVALID_REQUEST', '/products')

def test_malformed_json():
    error(call('POST', '/products', raw=b'{'), 400, 'INVALID_REQUEST', '/products')

def test_empty_body():
    error(call('POST', '/products', raw=b''), 400, 'INVALID_REQUEST', '/products')

def test_all_invalid_fields():
    p = payload()
    p.update(name='', stock=-1, price=0)
    error(call('POST', '/products', p), 400, 'VALIDATION_ERROR', '/products', ['name', 'stock', 'price'])

def test_duplicate_and_failed_update_atomic(created):
    p, b, _ = created
    path = f"/products/{b['id']}"
    error(call('POST', '/products', p), 409, 'DUPLICATE_SKU', '/products')
    for changes, status, code in [({'brandId': 999999}, 404, 'BRAND_NOT_FOUND'), ({'sku': 'MOB-001'}, 409, 'DUPLICATE_SKU'), ({'stock': None}, 400, 'VALIDATION_ERROR')]:
        bad = {**p, 'name': 'MUST NOT SAVE', **changes}
        error(call('PUT', path, bad), status, code, path, ['stock'] if status == 400 else None)
        _actual_34 = ok(call('GET', path), 200)
        _expected_34 = b
        _require(_actual_34 == _expected_34, "ok(call('GET', path), 200) == b", _expected_34, _actual_34)

@cases('price,stock,ram,storage', [(0.01, 0, 2, 32), (99999999.99, 100000, 24, 1024)])
def test_valid_boundaries(created, price, stock, ram, storage):
    p, b, _ = created
    p.update(price=price, stock=stock, ramGb=ram, storageGb=storage)
    updated = ok(call('PUT', f"/products/{b['id']}", p), 200)
    _actual_35 = (updated['price'], updated['stock'], updated['ramGb'], updated['storageGb'])
    _expected_35 = (price, stock, ram, storage)
    _require(_actual_35 == _expected_35, "(updated['price'], updated['stock'], updated['ramGb'], updated['storageGb']) == (price, stock, ram, storage)", _expected_35, _actual_35)

def test_trim(created):
    p, b, _ = created
    p.update(sku=' ' + p['sku'] + ' ', name=' Phone ', color=' Black ')
    updated = ok(call('PUT', f"/products/{b['id']}", p), 200)
    _require(updated['sku'] == p['sku'].strip() and updated['name'] == 'Phone' and (updated['color'] == 'Black'), "updated['sku'] == p['sku'].strip() and updated['name'] == 'Phone' and (updated['color'] == 'Black')", True, 'condition is false')

def test_transport_errors():
    before = ok(call('GET', '/products'), 200)
    error(call('POST', '/products', payload(), headers={'Content-Type': 'text/plain'}), 415, 'UNSUPPORTED_MEDIA_TYPE', '/products')
    error(call('POST', '/products', payload(), headers={'Accept': 'text/plain'}), 406, 'NOT_ACCEPTABLE', '/products')
    error(call('POST', '/brands', {}), 405, 'METHOD_NOT_ALLOWED', '/brands')
    _actual_37 = ok(call('GET', '/products'), 200)
    _expected_37 = before
    _require(_actual_37 == _expected_37, "ok(call('GET', '/products'), 200) == before", _expected_37, _actual_37)

def call(method, path, body=None, headers=None, raw=None):
    global LAST_REQUEST
    LAST_REQUEST = f'{method} {path}'
    if method == 'POST' and path == '/products' and isinstance(body, dict):
        sku = body.get('sku')
        if isinstance(sku, str):
            ATTEMPTED_SKUS.add(sku.strip(' '))
    result = _http(method, path, body, headers, raw)
    record_http(method, path, body, headers, raw, result)
    if method == 'POST' and path == '/products' and isinstance(result[2], dict):
        pid = result[2].get('id')
        if isinstance(pid, int) and (not isinstance(pid, bool)) and (pid > 0) and (pid not in BASELINE_IDS):
            TRACKED_IDS.add(pid)
    return result

def cleanup():
    problems = []
    try:
        rows = ok(call('GET', '/products'), 200)
        _require(isinstance(rows, list), 'isinstance(rows, list)', True, 'condition is false')
        for row in rows:
            if row.get('id') not in BASELINE_IDS and row.get('sku') in ATTEMPTED_SKUS:
                TRACKED_IDS.add(row['id'])
    except Exception as e:
        problems.append('cleanup discovery: ' + str(e))
    for pid in sorted(TRACKED_IDS):
        try:
            r = call('DELETE', f'/products/{pid}')
            if r[0] not in (204, 404):
                problems.append(f'cleanup DELETE id={pid}: HTTP {r[0]}')
        except Exception as e:
            problems.append(f'cleanup DELETE id={pid}: {e}')
    TRACKED_IDS.clear()
    ATTEMPTED_SKUS.clear()
    try:
        if ok(call('GET', '/products'), 200) != EXPECTED_PRODUCTS:
            problems.append('products do not match seed after cleanup; restart Spring Boot')
        if ok(call('GET', '/brands'), 200) != EXPECTED_BRANDS:
            problems.append('brands do not match seed after cleanup; restart Spring Boot')
    except Exception as e:
        problems.append('cleanup verification: ' + str(e))
    return problems

def group(name):
    if name in ('test_seed', 'test_filter'):
        return 'Read / seed / filter'
    if name in ('test_create_location', 'test_full_update_and_idempotence', 'test_delete', 'test_trim', 'test_valid_boundaries'):
        return 'CRUD / DTO'
    if name in ('test_validation', 'test_missing_required', 'test_invalid_types', 'test_all_invalid_fields'):
        return 'Validation / types'
    return 'Exceptions / HTTP'

def case_list():
    rows = []
    for name, fn in list(globals().items()):
        if name.startswith('test_') and callable(fn):
            values = getattr(fn, '_case_values', None)
            if values is None:
                rows.append((name, fn, {}))
            else:
                fields = fn._case_fields
                for v in values:
                    v = (v,) if len(fields) == 1 else v
                    args = dict(zip(fields, v))
                    rows.append((name + '[' + ','.join((repr(x) for x in v)) + ']', fn, args))
    return rows

def short_reason(e):
    if str(e):
        return str(e).replace('\n', ' ')[:250]
    frames = traceback.extract_tb(e.__traceback__)
    return frames[-1].line or type(e).__name__ if frames else type(e).__name__

def main():
    global BASE, TIMEOUT, BASELINE_IDS
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--base-url', default=os.environ.get('BASE_URL', 'http://localhost:8080'))
    parser.add_argument('--timeout', type=float, default=10)
    parser.add_argument('--summary', default='test_summary.json')
    parser.add_argument('--report', default='api_test_report.html', help='Readable HTML feedback report')
    parser.add_argument('--details', action='store_true', help='Print one short line for each failed case')
    parser.add_argument('--list', action='store_true', help='List cases without calling API')
    opts = parser.parse_args()
    BASE = opts.base_url.rstrip('/')
    TIMEOUT = opts.timeout
    if urlsplit(BASE).scheme not in ('http', 'https') or not urlsplit(BASE).netloc or urlsplit(BASE).path not in ('', '/'):
        parser.error('--base-url must be an origin, without /api/v1')
    if TIMEOUT <= 0:
        parser.error('--timeout must be positive')
    if not __debug__:
        parser.error('Do not use python -O: assertions are required')
    allcases = case_list()
    if opts.list:
        for name, _, _ in allcases:
            print(name)
        print(f'{len(allcases)} cases')
        return 0
    started = time.monotonic()
    results = []
    preflight = None
    aborted = False
    clean = False
    try:
        _actual_39 = ok(call('GET', '/brands'), 200)
        _expected_39 = EXPECTED_BRANDS
        _require(_actual_39 == _expected_39, "ok(call('GET', '/brands'), 200) == EXPECTED_BRANDS", _expected_39, _actual_39)
        rows = ok(call('GET', '/products'), 200)
        _actual_40 = rows
        _expected_40 = EXPECTED_PRODUCTS
        _require(_actual_40 == _expected_40, 'rows == EXPECTED_PRODUCTS', _expected_40, _actual_40)
        BASELINE_IDS = {p['id'] for p in rows}
    except Exception as e:
        preflight = short_reason(e)
        preflight_diagnostic = dict(FAILURE)
        preflight_requests = list(TRACE)
    if preflight is None:
        for label, fn, kwargs in allcases:
            TRACE.clear()
            RESPONSE_INDEX.clear()
            reset_failure()
            tick = time.monotonic()
            status = 'PASSED'
            reason = ''
            request = None
            try:
                if 'created' in inspect.signature(fn).parameters:
                    p = payload()
                    r = call('POST', '/products', p)
                    b = ok(r, 201)
                    kwargs = {**kwargs, 'created': (p, b, r)}
                fn(**kwargs)
            except Exception as e:
                status = 'FAILED' if isinstance(e, AssertionError) else 'ERROR'
                reason = short_reason(e)
                request = LAST_REQUEST
            finally:
                evidence = list(TRACE)
                diagnostic = dict(FAILURE)
                issues = cleanup()
            if issues:
                status = 'ERROR'
                reason = (reason + '; ' if reason else '') + ' | '.join(issues)
                aborted = True
            results.append(dict(case=label, group=group(fn.__name__), status=status, reason=reason, request=request, seconds=round(time.monotonic() - tick, 3), requests=evidence, diagnostic=diagnostic, cleanupErrors=issues, hint=advice(fn.__name__, reason)))
            print_case(results[-1], len(results))
            if aborted:
                break
        clean = not aborted
    totals = {s: sum((r['status'] == s for r in results)) for s in ('PASSED', 'FAILED', 'ERROR')}
    report = dict(preflightDiagnostic=locals().get('preflight_diagnostic', {}), preflightRequests=locals().get('preflight_requests', []), baseUrl=BASE, planned=len(allcases), executed=len(results), notRun=len(allcases) - len(results), **totals, seedRestored=clean, preflightError=preflight, aborted=aborted, seconds=round(time.monotonic() - started, 2), results=results)
    try:
        Path(opts.summary).write_text(json.dumps(report, ensure_ascii=False, indent=2, default=str), encoding='utf-8')
    except OSError as e:
        print(f'Cannot save summary: {e}', file=sys.stderr)
        return 2
    write_html(report, opts.report)
    print('REST API TEST SUMMARY')
    if preflight:
        print('Preflight ERROR: ' + preflight)
    groups = defaultdict(lambda: defaultdict(int))
    for r in results:
        groups[r['group']][r['status']] += 1
    print(f"{'Group':26} {'PASS':>5} {'FAIL':>5} {'ERROR':>5}")
    for title, counts in groups.items():
        print(f"{title:26} {counts['PASSED']:5} {counts['FAILED']:5} {counts['ERROR']:5}")
    print(f"Total {len(results)}/{len(allcases)} | Passed {totals['PASSED']} | Failed {totals['FAILED']} | Errors {totals['ERROR']} | Not run {report['notRun']}")
    print(f"Seed restored: {('YES' if clean else 'NOT VERIFIED / FAILED')} | Time: {report['seconds']}s")
    print(f'Details: {opts.summary} | HTML: {opts.report}')
    if opts.details:
        for r in results:
            if r['status'] != 'PASSED':
                print(f"{r['status']}: {r['case']}: {r['reason']}")
    return 2 if preflight or aborted or totals['ERROR'] else 1 if totals['FAILED'] else 0


import html
TRACE=[]
RESPONSE_INDEX={}
FAILURE={}

class TestMismatch(AssertionError):
    pass

def reset_failure():
    FAILURE.clear()

def brief(value,limit=450):
    s=json.dumps(value,ensure_ascii=False,default=str)
    return s if len(s)<=limit else s[:limit]+'... (ดูรายละเอียดเต็มใน JSON)'

def first_difference(expected,actual,path='$'):
    if type(expected)!=type(actual):
        # API prices are compared numerically; JSON int/float are equivalent here.
        if type(expected) in (int,float) and type(actual) in (int,float) and expected==actual:return None
        return f'{path}: คาดหวังชนิด {type(expected).__name__}, ได้ {type(actual).__name__}'
    if isinstance(expected,dict):
        missing=set(expected)-set(actual);extra=set(actual)-set(expected)
        if missing or extra:return f'{path}: ขาด keys {sorted(missing)}, มี keys เกิน {sorted(extra)}'
        for k in expected:
            d=first_difference(expected[k],actual[k],path+'.'+k)
            if d:return d
    elif isinstance(expected,list):
        if len(expected)!=len(actual):return f'{path}: คาดหวัง {len(expected)} รายการ, ได้ {len(actual)} รายการ'
        for i,(e,a) in enumerate(zip(expected,actual)):
            d=first_difference(e,a,f'{path}[{i}]')
            if d:return d
    elif expected!=actual:return f'{path}: expected={brief(expected,120)}, actual={brief(actual,120)}'
    return None

def _require(condition,check,expected,actual):
    if condition:return
    FAILURE.update(check=check,expected=expected,actual=actual)
    diff=first_difference(expected,actual)
    reason=diff or f'เงื่อนไขไม่ผ่าน: {check}; expected={brief(expected)}, actual={brief(actual)}'
    FAILURE['difference']=reason
    raise TestMismatch(reason)

def record_http(method,path,body,headers,raw,result):
    row=dict(method=method,endpoint=PREFIX+path,requestBody=body,
             rawBody=raw.decode('utf-8',errors='replace') if raw is not None else None,
             requestHeaders=headers or {},actualStatus=result[0],expectedStatus=None,
             responseBody=result[2],responseContentType=result[1].get('Content-Type'),
             location=result[1].get('Location'))
    TRACE.append(row);RESPONSE_INDEX[id(result)]=row

def mark_expected(result,status):
    row=RESPONSE_INDEX.get(id(result))
    if row is not None:row['expectedStatus']=status

ADVICE={
 'test_seed':'ตรวจ GET /brands และ GET /products: ส่ง JSON array, เรียง id ASC และใช้ข้อมูลตั้งต้นตามโจทย์ รวม active=false',
 'test_filter':'ตรวจการรับ brandId และ filter ใน service/repository; brand ที่ไม่มีสินค้าต้องได้ []',
 'test_create_location':'ตรวจ POST: ใช้ 201, คืน ProductResponse และ Location ที่ชี้สินค้าที่เพิ่งสร้าง; ไม่กำหนด id เอง',
 'test_full_update_and_idempotence':'ตรวจ PUT ให้แทนค่าทุก field; description ที่ขาดต้องเป็น null; id เดิมและไม่เพิ่ม row เมื่อเรียกซ้ำ',
 'test_delete':'ตรวจ DELETE: คืน 204 ไม่มี body และลบจริง; GET/DELETE ซ้ำต้องได้ 404 PRODUCT_NOT_FOUND',
 'test_validation':'ตรวจ validation ของ request DTO และ @Valid; field ที่ผิดต้องปรากฏใน fieldErrors; อย่า save ข้อมูลก่อนตรวจครบ',
 'test_missing_required':'ตรวจ required fields ด้วย wrapper types เช่น Integer/Boolean และ @NotNull; ห้ามใช้ default แทน field ที่ไม่ได้ส่ง',
 'test_invalid_types':'ตรวจ Jackson coercion และ unknown properties; ไม่รับ string แทน number/boolean หรือ field ที่ไม่อยู่ใน DTO; คืน 400 INVALID_REQUEST',
 'test_all_invalid_fields':'รวม validation errors ทุก field ใน fieldErrors; ไม่หยุดที่ field แรก',
 'test_missing_product':'ตรวจ service เมื่อหา id ไม่พบ และ global handler ให้คืน 404 PRODUCT_NOT_FOUND',
 'test_invalid_path_query':'ตรวจ path/query ให้เป็น positive integer; ปฏิเสธ parameter แปลก/ซ้ำ/overflow ด้วย 400 INVALID_REQUEST',
 'test_missing_brand_filter':'ตรวจ brand ก่อน filter; ถ้า brand ไม่มีให้ 404 BRAND_NOT_FOUND',
 'test_malformed_json':'จัดการ HttpMessageNotReadableException ให้คืน 400 INVALID_REQUEST ตาม error schema',
 'test_empty_body':'จัดการ body ว่างให้คืน 400 INVALID_REQUEST',
 'test_duplicate_and_failed_update_atomic':'ตรวจ SKU ซ้ำและ brand ไม่พบก่อน save; failed PUT ต้องไม่เปลี่ยนข้อมูลบางส่วน และใช้ transaction ตามความเหมาะสม',
 'test_valid_boundaries':'ขอบเขตต้องเป็น inclusive; ใช้ BigDecimal สำหรับราคา และอนุญาต RAM/storage ตามโจทย์',
 'test_trim':'trim sku/name/color ก่อน validate/save; ห้ามตรวจ SKU ซ้ำโดยรวมช่องว่างส่วนเกิน',
 'test_transport_errors':'ตรวจ 405/415/406 และ error schema; คำขอที่ Accept ไม่รองรับต้องไม่เขียนข้อมูล',
}

def advice(name,reason):
    return ADVICE.get(name,'ตรวจ controller, DTO mapping และ global exception handler เทียบกับ expected/actual ในรายงาน')

def print_case(row,index):
    checked=[r for r in row['requests'] if r.get('expectedStatus') is not None]
    endpoints=list(dict.fromkeys(r['method']+' '+r['endpoint'] for r in row['requests']))
    print(f'[{row["status"]}] {index:02} {row["case"]}')
    print('  Endpoint: '+(' | '.join(endpoints) if endpoints else row.get('request') or 'ยังเรียก endpoint ไม่สำเร็จ'))
    if row['status']=='PASSED':
        print('  ผ่าน: HTTP status และเงื่อนไขของกรณีนี้ตรงตามโจทย์')
    else:
        print('  เหตุผล: '+row['reason'])
        if row['diagnostic']:
            print('  Expected: '+brief(row['diagnostic'].get('expected')))
            print('  Actual:   '+brief(row['diagnostic'].get('actual')))
        for r in checked:
            if r['expectedStatus']!=r['actualStatus']:
                print(f'  HTTP: expected {r["expectedStatus"]}, actual {r["actualStatus"]}')
        print('  แนวทางตรวจ: '+row['hint'])

def write_html(report,path):
    def esc(x):return html.escape(str(x))
    def pretty(x):return esc(json.dumps(x,ensure_ascii=False,indent=2,default=str))
    parts=['<!doctype html><html lang="th"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>REST API Feedback</title>',
           '<style>body{font:16px system-ui,sans-serif;max-width:1100px;margin:30px auto;padding:0 18px;background:#f6f8fb;color:#16243a}section{background:white;border:1px solid #d7dfeb;border-radius:10px;margin:14px 0;padding:18px}pre{white-space:pre-wrap;overflow-wrap:anywhere;background:#f2f4f8;padding:12px}td,th{padding:8px;text-align:left;border-bottom:1px solid #dde3eb}.PASSED{color:#137544}.FAILED,.ERROR{color:#b52332}summary{cursor:pointer}code{overflow-wrap:anywhere}</style>',
           '<h1>REST API — รายงานช่วยแก้โปรแกรม</h1>',f'<p>{esc(report["baseUrl"])} · ตรวจ {report["executed"]}/{report["planned"]} · ผ่าน {report["PASSED"]} · ไม่ผ่าน {report["FAILED"]} · ERROR {report["ERROR"]} · ยังไม่รัน {report["notRun"]}</p>',
           '<p>แนวทางตรวจเป็นข้อเสนอจาก requirement ของกรณีทดสอบ ไม่ใช่ข้อยืนยันต้นเหตุจาก source code; รายงานนี้เรียก HTTP API เท่านั้น</p>']
    if report['preflightError']:
        parts+=['<section><h2 class="ERROR">Preflight ERROR</h2><p>'+esc(report['preflightError'])+'</p><pre>'+pretty(report.get('preflightDiagnostic'))+'</pre><p>ตรวจว่าแอปเปิดอยู่, base URL ถูกต้อง และ GET คืนข้อมูลเริ่มต้นตามโจทย์ รายการทดสอบยังไม่ได้เริ่ม</p></section>']
    for row in report['results']:
        parts+=['<section>',f'<h2 class="{esc(row["status"])}">{esc(row["status"])} — {esc(row["case"])}</h2>',
                '<p>'+esc('ผ่านเงื่อนไขของกรณีนี้' if row['status']=='PASSED' else row['reason'])+'</p>']
        if row['status']!='PASSED':
            parts+=['<h3>Expected / Actual</h3><pre>'+pretty(row['diagnostic'])+'</pre>','<p><strong>แนวทางตรวจ:</strong> '+esc(row['hint'])+'</p>']
        parts+=['<table><tr><th>Method / Endpoint</th><th>Expected HTTP</th><th>Actual HTTP</th></tr>']
        for r in row['requests']:
            parts+=['<tr><td>'+esc(r['method']+' '+r['endpoint'])+'</td><td>'+esc(r['expectedStatus'] if r['expectedStatus'] is not None else 'ไม่ได้ตรวจ status แยก')+'</td><td>'+esc(r['actualStatus'])+'</td></tr>']
        parts+=['</table><details><summary>ดู request / response ของกรณีนี้</summary>']
        for r in row['requests']:parts+=['<h3>'+esc(r['method']+' '+r['endpoint'])+'</h3><pre>'+pretty(r)+'</pre>']
        parts+=['</details></section>']
    parts+=['<p>Seed restored: '+esc(report['seedRestored'])+' · cleanup ใช้ DELETE API เฉพาะข้อมูลที่ test สร้าง</p></html>']
    try:Path(path).write_text('\n'.join(parts),encoding='utf-8')
    except OSError as e:raise SystemExit('Cannot write HTML report: '+str(e))


if __name__ == '__main__':
    raise SystemExit(main())
