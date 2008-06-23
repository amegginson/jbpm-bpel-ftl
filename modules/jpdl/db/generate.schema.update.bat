@echo off
ant -f build.upgrade.xml generate.old.schema
ant -f build.upgrade.xml generate.update > target/schema.upgrade.log
echo schema update generated in target/schema.upgrade.log