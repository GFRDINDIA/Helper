@echo off
SET PATH=C:\Program Files\nodejs;%PATH%
cd /d C:\Users\ASUS\Documents\Work\GFRND\Dev\Helper\helper-admin-panel
echo Node version:
node --version
echo NPM version:
npm --version
echo Installing dependencies...
npm install
echo Done.
