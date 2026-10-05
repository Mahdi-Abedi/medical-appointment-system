#!/bin/bash
set -e

echo "==============================================="
echo "Starting Oracle init script for users..."
echo "==============================================="

sqlplus -s sys/${ORACLE_PASSWORD}@localhost:1521/XE as sysdba <<EOF
ALTER SESSION SET CONTAINER = XEPDB1;

-- ایجاد Tablespace
CREATE TABLESPACE doctor_ts DATAFILE 'doctor01.dbf' SIZE 100M AUTOEXTEND ON;
CREATE TABLESPACE appointment_ts DATAFILE 'appointment01.dbf' SIZE 100M AUTOEXTEND ON;

-- کاربر Doctor
CREATE USER doctor IDENTIFIED BY doctor
  DEFAULT TABLESPACE doctor_ts
  QUOTA UNLIMITED ON doctor_ts;
GRANT CONNECT, RESOURCE, CREATE SESSION TO doctor;

-- کاربر Appointment
CREATE USER appointment IDENTIFIED BY appointment
  DEFAULT TABLESPACE appointment_ts
  QUOTA UNLIMITED ON appointment_ts;
GRANT CONNECT, RESOURCE, CREATE SESSION TO appointment;

EXIT;
EOF

echo "==============================================="
echo "Appointment user created successfully!"
echo "==============================================="