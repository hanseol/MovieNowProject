<?php
      $dbc= mysqli_connect('localhost', 'root', '133nplab', 'moona')
        or die($dbc->error);


      $query="DROP TABLE moona.m_time";
      $result = mysqli_query($dbc, $query)
        or die($dbc->error);

      $query="DROP TABLE moona.m_cur_infor";
      $result = mysqli_query($dbc, $query)
        or die($dbc->error);

      $query="DROP TABLE moona.m_exp_infor";
      $result = mysqli_query($dbc, $query)
        or die($dbc->error);


      $query="CREATE TABLE `m_time` (
  `time_no` varchar(10) NOT NULL,
  `movie` varchar(50) DEFAULT NULL,
  `theater_no` int DEFAULT NULL,
  `time` varchar(10) DEFAULT NULL,
  `reservation_url` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`time_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8";
      $result = mysqli_query($dbc, $query)
        or die($dbc->error);


        $query="CREATE TABLE `moona`.`m_cur_infor` (
           `no` INT NOT NULL,
           `name` VARCHAR(60) NULL,
           `thumbnail` VARCHAR(200) NULL,
           `grade` DOUBLE NULL,
           `genre` VARCHAR(50) NULL,
           `run_time` VARCHAR(10) NULL,
           `r_date` DATE NULL,
           `director` VARCHAR(60) NULL,
           `actor` VARCHAR(80) NULL,
           `rating` VARCHAR(30) NULL,
           `story` LONGTEXT NULL,
           PRIMARY KEY (`no`))
         ENGINE = InnoDB
         DEFAULT CHARACTER SET = utf8";
        $result = mysqli_query($dbc, $query)
          or die($dbc->error);


        $query="CREATE TABLE `moona`.`m_exp_infor` (
          `no` INT NOT NULL,
          `name` VARCHAR(60) NULL,
          `thumbnail` VARCHAR(200) NULL,
          `genre` VARCHAR(50) NULL,
          `run_time` VARCHAR(10) NULL,
          `r_date` DATE NULL,
          `director` VARCHAR(60) NULL,
          `actor` VARCHAR(80) NULL,
          `rating` VARCHAR(30) NULL,
          `story` LONGTEXT NULL,
          PRIMARY KEY (`no`))
        ENGINE = InnoDB
        DEFAULT CHARACTER SET = utf8";
        $result = mysqli_query($dbc, $query)
          or die($dbc->error);


         mysqli_close($dbc);
   ?>
