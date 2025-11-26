create table if not exists category (
                                        id          integer not null auto_increment,
                                        description varchar(255),
    name        varchar(255),
    primary key (id)
    );

create table if not exists products (
                                       id                 integer          not null auto_increment,
                                       available_quantity double precision not null,
                                       description        varchar(255),
    name               varchar(255),
    price              decimal(38, 2),
    category_id        integer,
    primary key (id),
    constraint fk1mtsbur82frn64de7balymq9s foreign key (category_id) references category(id)
    );

