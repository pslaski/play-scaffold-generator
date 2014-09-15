play-scaffold-generator
=================

SBT plugin which generates CRUD for Play Framework 2.3.x with Anorm, Squeryl or Slick database access library

Installation
------------

Add the following line to **/project/plugins.sbt** file:

    addSbtPlugin("com.github.pslaski" % "play-scaffold-generator" % "1.0.0")

Configuration
-----

### H2 database in-memory mode

Create database initialization script and use `INIT` with `RUNSCRIPT FROM` to execute it on connection.

Example database configuration for H2 database engine in an in-memory mode:

    db.default.driver=org.h2.Driver
    db.default.url="jdbc:h2:mem:play;INIT=runscript from 'conf/evolutions/default/create.sql'"

### Other databases

Configure a default connection pool in the **conf/application.conf** file.

Example configuration for PostgreSQL:

    db.default.driver=org.postgresql.Driver
    db.default.url="jdbc:postgresql://database.example.com/playdb"
    db.default.user=dbuser
    db.default.password="12345"
    
### Scaffold configuration

Create **conf/scaffold-config.conf** file with additional scaffold configuration using [HOCON](https://github.com/typesafehub/config/blob/master/HOCON.md) (Human-Optimized Config Object Notation).

A file must contains `tables` field with an array of objects describing each table.
Every object must have `table-name` property defined. Rest of properties is optional.

Possible properties:

* `table-name`
* `list-columns` - array with the names of columns, which will be showed in a table list view. Default value is first five columns.
* `select-columns` - array with the names of columns, which will be showed in a select tag. Default value is first five columns.
* `is-junction-table` - boolean value, which marks a junction table. Tables consisting only two foreign key columns without primary key are automatic marked as junction tables.

Example configuration file:

    tables = [
      {table-name : Player, 
        list-columns : [name, surname, birth], 
        select-columns : [surname, name] },
      {table-name : Arena, 
        list-columns : [name, city], 
        select-columns : [city, name] },
      {table-name : Arena_Sport_Club, 
        is-junction-table : true},
      {table-name : Sport_Club, 
        list-columns : [name, city], 
        select-columns : [name, city] }
    ]

Usage
----------------------

After installation and configuration start or reload Activator project console.

Run **scaffoldAnorm**, **scaffoldSqueryl** or **ScaffoldSlick** command to generate all files.

### License

This software is licensed under the Apache 2 license ( http://www.apache.org/licenses/LICENSE-2.0 ).