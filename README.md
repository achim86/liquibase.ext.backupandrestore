# Liquibase Backup And Restore Extension

Extends Liquibase a great tool to manage and apply database changes by providing backup and restore support for whole tables (structure and data).

![fileurls](http://img826.imageshack.us/img826/1452/booma.png)
	  
# Usage

## Classpath
Add liquibase-backup-and-restore-x.x.jar to your classpath.

## Namespace
![fileurls](http://img29.imageshack.us/img29/5824/namespace.png)

# Running from your IDE
Due to licensing you have to add [ojdbc*.jar](http://www.oracle.com/technetwork/database/enterprise-edition/jdbc-112010-090769.html) to lib_additional folder and add it to your classpath first.

# Contribute
Currently there is only oracle support. If you are using other dbms feel free to add support. There is an [interface](https://github.com/achim86/liquibase.ext.backupandrestore/blob/master/liquibase.ext.backupandrestore/JavaSource/liquibase/ext/backupandrestore/BackupRestore.java) 
which you can implement for your specific DBMS.

# Running Tests
If you want to run the tests create two schemas TEST//123 TEST_2//123 with the following grants

	GRANT CREATE SESSION TO TEST;
	GRANT UNLIMITED TABLESPACE TO TEST;
	GRANT CREATE ANY TABLE TO TEST;
	GRANT SELECT ANY TABLE TO TEST;
	GRANT UPDATE ANY TABLE TO TEST;
	GRANT ALTER ANY TABLE TO TEST;
	GRANT DROP ANY TABLE TO TEST;
	GRANT INSERT ANY TABLE TO TEST;
	GRANT SELECT_CATALOG_ROLE TO TEST;
		
	GRANT CREATE SESSION TO TEST_2;
	GRANT UNLIMITED TABLESPACE TO TEST_2;
	GRANT CREATE ANY TABLE TO TEST_2;
	GRANT SELECT ANY TABLE TO TEST_2;
	GRANT UPDATE ANY TABLE TO TEST_2;
	GRANT ALTER ANY TABLE TO TEST_2;
	GRANT DROP ANY TABLE TO TEST_2;
	GRANT INSERT ANY TABLE TO TEST_2;
  
# Used libraries	  
	  
Most libraries are just for the test cases

- Liquibase http://www.liquibase.org/
- Liquibase Log4J Extension https://liquibase.jira.com/wiki/pages/viewpage.action?pageId=7634962
- Apache Log4j http://logging.apache.org/log4j/
- Apache Commons Logging http://commons.apache.org/logging/
- Spring Framework http://www.springsource.org/
- JUnit http://www.junit.org/