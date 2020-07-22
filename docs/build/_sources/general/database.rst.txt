Database and Storage
====================

PetBlocks uses a SQLite database per default which is the file called PetBlocks.db inside of the PetBlocks plugin folder.

If you rename or delete the file it gets automatically regenerated but all of your saves are lost. So it might be
useful to copy the PetBlocks.db file from time to time in order to create backups.

One option you should consider is using a MySQL database instead of a SQLite database.

Using the MySQL database configuration is recommend for:

* A large player base
* Performance
* There is one available (included in the hosting package)

**config.yml**
::
    ############################

    # Database settings

    # All pets are stored in a SQLite database per default which is basically the
    # PetBlocks.db file in the plugin folder.
    # Available types are 'sqlite' and 'mysql'.
    # Use the same mysql database for 2 or more server in order to allow cross server pets.

    ############################

    sql:
      type: 'mysql'
      host: 'yourHostName'
      port: 3306
      database: 'yourDbName'
      usessl: true
      username: 'yourUserName'
      password: 'yourPassword'

