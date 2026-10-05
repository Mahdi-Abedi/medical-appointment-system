#!/bin/bash
set -e

echo "==============================================="
echo "Starting Oracle init script for appointment user..."
echo "==============================================="

sqlplus -s sys/${ORACLE_PASSWORD}@localhost:1521/XE as sysdba <<EOF
ALTER SESSION SET CONTAINER = XEPDB1;

-- Tablespace for Appointment
CREATE TABLESPACE appointment_ts
  DATAFILE 'appointment01.dbf' SIZE 100M AUTOEXTEND ON;

-- Appointment user
CREATE USER appointment IDENTIFIED BY appointment
  DEFAULT TABLESPACE appointment_ts
  QUOTA UNLIMITED ON appointment_ts;

GRANT CONNECT, RESOURCE, CREATE SESSION TO appointment;

EXIT;
EOF

echo "==============================================="
echo "Appointment user created successfully!"
echo "==============================================="