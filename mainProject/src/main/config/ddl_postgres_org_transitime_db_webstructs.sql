
    drop table if exists ApiKeys cascade;

    drop table if exists WebAgencies cascade;

    create table ApiKeys (
        applicationName varchar(80) not null,
        applicationKey varchar(20),
        applicationUrl varchar(80),
        description varchar(1000),
        email varchar(80),
        phone varchar(80),
        primary key (applicationName)
    );

    create table WebAgencies (
        agencyId varchar(60) not null,
        hostName varchar(120),
        primary key (agencyId)
    );
