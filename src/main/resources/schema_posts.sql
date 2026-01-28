-- Таблица с постами
create table if not exists posts(
    id bigserial primary key,
    title varchar(256) not null,
    text varchar(256) not null,
    likes_count integer not null,
    comments_count integer not null
    );

-- Таблица тегов
create table if not exists tags(
  id bigserial primary key,
  name varchar(50) not null unique
    );

-- Связь постов и тегов
create table if not exists post_tags(
    post_id bigint not null,
    tag_id bigint not null,
    primary key (post_id, tag_id),
    foreign key (post_id) references posts(id) on delete cascade,
    foreign key (tag_id) references tags(id) on delete cascade
    );

-- инициализация таблицы тегов
insert into tags(name) values ('tag_1');
insert into tags(name) values ('tag_2');
insert into tags(name) values ('tag_3');

insert into posts(title, text, likes_count, comments_count) values ('t_title_1 aaa', 'text_1', 1, 1);
insert into posts(title, text, likes_count, comments_count) values ('t_title_12 aaa', 'text_2', 2, 2);
insert into posts(title, text, likes_count, comments_count) values ('t_title_13 bbb', 'text_3', 3, 3);

-- связь постов с тегами
insert into post_tags(post_id, tag_id) values (1, (select id from tags where name = 'tag_1'));
insert into post_tags(post_id, tag_id) values (1, (select id from tags where name = 'tag_2'));
insert into post_tags(post_id, tag_id) values (1, (select id from tags where name = 'tag_3'));

insert into post_tags(post_id, tag_id) values (2, (select id from tags where name = 'tag_2'));

insert into post_tags(post_id, tag_id) values (3, (select id from tags where name = 'tag_3'));

