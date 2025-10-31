CREATE TABLE IF NOT EXISTS locations (
     code varchar(10) NOT NULL primary key,
     city_name varchar(128) NOT NULL,
     country_code varchar(10) NOT NULL,
     country_name varchar(64) NOT NULL,
     enabled bool ,
     region_name varchar(128) DEFAULT NULL,
     trashed bool,
    PRIMARY KEY (code));

INSERT INTO locations(code, city_name, country_code, country_name, enabled, region_name, trashed) VALUES
('DELHI_IN','Delhi','IN','India',TRUE,'Delhi',FALSE),
('LACA_USA','Los Angeles','US','United States of America',TRUE,'California',FALSE),
('NYC_USA','New York City','US','United States of America',TRUE,'New York',FALSE),
('MADRID_ES','Madrid','ES','Spain',TRUE,'Community of Madrid',FALSE);

CREATE TABLE IF NOT EXISTS realtime_weather (
    location_code varchar(10) not null primary key,
    humidity      int         not null,
    last_updated  datetime(6) null,
    precipitation int         not null,
    status        varchar(50) null,
    temperature   int         not null,
    wind_speed    int         not null,
    foreign key (location_code) references locations (code)
);

INSERT INTO realtime_weather(location_code, humidity, last_updated, precipitation, status, temperature, wind_speed) VALUES ('DELHI_IN',75,'2025-06-08 20:28',95,'Raining',35,35),('NYC_USA',60,'2025-06-08 20:45',100,'Snowing',-20,18);

CREATE TABLE IF NOT EXISTS weather_hourly (
      location_code  varchar(10) NOT NULL,
      hour_of_day int NOT NULL,
      temperature int NOT NULL,
      precipitation int NOT NULL,
      status varchar(50) NOT NULL,
      PRIMARY KEY (location_code,hour_of_day),
      foreign key (location_code) references locations (code)
);

INSERT INTO weather_hourly(location_code, hour_of_day, temperature, precipitation, status) VALUES ('DELHI_IN',8,23,75,'Cloudy'),('DELHI_IN',9,21,95,'Raining'),('DELHI_IN',10,25,25,'Sunny'),('NYC_USA',5,18,30,'Clear'),('NYC_USA',6,20,50,'Cloudy');

CREATE TABLE IF NOT EXISTS weather_daily (
      location_code  varchar(10) NOT NULL,
      day_of_month int NOT NULL,
      month int NOT NULL,
      max_temperature int NOT NULL,
      min_temperature int NOT NULL,
      precipitation int NOT NULL,
      status varchar(50) NULL,
      PRIMARY KEY (location_code,day_of_month,month)

);

INSERT INTO weather_daily(location_code,day_of_month,month, max_temperature,min_temperature, precipitation, status) VALUES ('DELHI_IN',10,5,33,27,20,'Clear'),('DELHI_IN',11,5,28,24,60,'Cloudy'),('DELHI_IN',12,5,23,19,95,'Rain'),('NYC_USA',2,5,19,15,80,'Cloudy'),('NYC_USA',3,5,26,22,30,'Clear');
