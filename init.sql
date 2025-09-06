-- ViceCode Database Schema - Improved Version
-- Generated on: September 5, 2025
-- Fixes circular dependencies, data type mismatches, and adds proper relationships

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema videcode
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `videcode` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `videcode`;

-- -----------------------------------------------------
-- Table `videcode`.`users`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `videcode`.`users` (
  `id` CHAR(36) NOT NULL,
  `email` VARCHAR(320) NOT NULL, -- Changed from TEXT to proper email length
  `reg_no` VARCHAR(100) NOT NULL, -- Changed from TEXT to reasonable length
  `password` VARCHAR(255) NOT NULL, -- Proper length for hashed passwords
  `name` VARCHAR(255) NOT NULL,
  `role` ENUM('student', 'admin', 'interviewer') NOT NULL DEFAULT 'student',
  `round_qualified` INT NOT NULL DEFAULT 0,
  `score` INT NOT NULL DEFAULT 0,
  `is_banned` TINYINT(1) NOT NULL DEFAULT 0,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `unique_email` (`email`),
  UNIQUE INDEX `unique_reg_no` (`reg_no`),
  INDEX `idx_role` (`role`),
  INDEX `idx_round_qualified` (`round_qualified`),
  INDEX `idx_score` (`score`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- -----------------------------------------------------
-- Table `videcode`.`companies`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `videcode`.`companies` (
  `id` CHAR(36) NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  `logo` TEXT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_company_name` (`name`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- -----------------------------------------------------
-- Table `videcode`.`questions`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `videcode`.`questions` (
  `id` CHAR(36) NOT NULL,
  `title` VARCHAR(500) NOT NULL,
  `description` TEXT NOT NULL,
  `input_format` TEXT NULL,
  `output_format` TEXT NOT NULL,
  `constraints` TEXT NOT NULL,
  `sample_test_input` TEXT NULL,
  `sample_test_output` TEXT NULL,
  `explanation` TEXT NULL,
  `points` INT NOT NULL DEFAULT 0,
  `round` INT NOT NULL DEFAULT 1,
  `difficulty` ENUM('easy', 'medium', 'hard') NOT NULL DEFAULT 'medium',
  `time_limit` DECIMAL(5,2) NOT NULL DEFAULT 2.00, -- in seconds
  `memory_limit` DECIMAL(8,2) NOT NULL DEFAULT 256.00, -- in MB
  `is_active` TINYINT(1) NOT NULL DEFAULT 1,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_round` (`round`),
  INDEX `idx_difficulty` (`difficulty`),
  INDEX `idx_points` (`points`),
  INDEX `idx_active` (`is_active`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- -----------------------------------------------------
-- Table `videcode`.`testcases`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `videcode`.`testcases` (
  `id` CHAR(36) NOT NULL,
  `question_id` CHAR(36) NOT NULL,
  `input` TEXT NOT NULL,
  `expected_output` TEXT NOT NULL,
  `is_hidden` TINYINT(1) NOT NULL DEFAULT 1,
  `time_limit` DECIMAL(5,2) NOT NULL DEFAULT 2.00,
  `memory_limit` DECIMAL(8,2) NOT NULL DEFAULT 256.00,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_question_id` (`question_id`),
  INDEX `idx_hidden` (`is_hidden`),
  CONSTRAINT `fk_testcases_question`
    FOREIGN KEY (`question_id`)
    REFERENCES `videcode`.`questions` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- -----------------------------------------------------
-- Table `videcode`.`user_stats`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `videcode`.`user_stats` (
  `id` CHAR(36) NOT NULL,
  `user_id` CHAR(36) NOT NULL,
  `question_id` CHAR(36) NOT NULL,
  `solved_at` TIMESTAMP NULL,
  `attempts` INT NOT NULL DEFAULT 0,
  `best_score` INT NOT NULL DEFAULT 0,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `unique_user_question` (`user_id`, `question_id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_question_id` (`question_id`),
  INDEX `idx_solved_at` (`solved_at`),
  CONSTRAINT `fk_user_stats_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `videcode`.`users` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_user_stats_question`
    FOREIGN KEY (`question_id`)
    REFERENCES `videcode`.`questions` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- -----------------------------------------------------
-- Table `videcode`.`interviews`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `videcode`.`interviews` (
  `id` CHAR(36) NOT NULL,
  `user_id` CHAR(36) NOT NULL,
  `company_id` CHAR(36) NOT NULL,
  `description` TEXT NULL,
  `scheduled_date` DATETIME NOT NULL,
  `status` ENUM('scheduled', 'completed', 'cancelled', 'no_show') NOT NULL DEFAULT 'scheduled',
  `score` INT NULL,
  `feedback` TEXT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_company_id` (`company_id`),
  INDEX `idx_scheduled_date` (`scheduled_date`),
  INDEX `idx_status` (`status`),
  CONSTRAINT `fk_interviews_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `videcode`.`users` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_interviews_company`
    FOREIGN KEY (`company_id`)
    REFERENCES `videcode`.`companies` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- -----------------------------------------------------
-- Table `videcode`.`programming_languages`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `videcode`.`programming_languages` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  `version` VARCHAR(20) NULL,
  `file_extension` VARCHAR(10) NOT NULL,
  `is_active` TINYINT(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `unique_name` (`name`),
  INDEX `idx_active` (`is_active`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- -----------------------------------------------------
-- Table `videcode`.`submissions`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `videcode`.`submissions` (
  `id` CHAR(36) NOT NULL,
  `user_id` CHAR(36) NOT NULL,
  `question_id` CHAR(36) NOT NULL,
  `language_id` INT NOT NULL,
  `source_code` TEXT NOT NULL,
  `status` ENUM('pending', 'running', 'accepted', 'wrong_answer', 'time_limit_exceeded', 'memory_limit_exceeded', 'runtime_error', 'compilation_error') NOT NULL DEFAULT 'pending',
  `testcases_passed` INT NOT NULL DEFAULT 0,
  `testcases_failed` INT NOT NULL DEFAULT 0,
  `total_testcases` INT NOT NULL DEFAULT 0,
  `execution_time` DECIMAL(8,2) NULL, -- in milliseconds
  `memory_used` DECIMAL(8,2) NULL, -- in MB
  `score` INT NOT NULL DEFAULT 0,
  `submission_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_question_id` (`question_id`),
  INDEX `idx_language_id` (`language_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_submission_time` (`submission_time`),
  CONSTRAINT `fk_submissions_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `videcode`.`users` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_submissions_question`
    FOREIGN KEY (`question_id`)
    REFERENCES `videcode`.`questions` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_submissions_language`
    FOREIGN KEY (`language_id`)
    REFERENCES `videcode`.`programming_languages` (`id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- -----------------------------------------------------
-- Table `videcode`.`submission_results`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `videcode`.`submission_results` (
  `id` CHAR(36) NOT NULL,
  `submission_id` CHAR(36) NOT NULL,
  `testcase_id` CHAR(36) NOT NULL,
  `status` ENUM('accepted', 'wrong_answer', 'time_limit_exceeded', 'memory_limit_exceeded', 'runtime_error') NOT NULL,
  `execution_time` DECIMAL(8,2) NULL, -- in milliseconds
  `memory_used` DECIMAL(8,2) NULL, -- in MB
  `output` TEXT NULL,
  `error_message` TEXT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_submission_id` (`submission_id`),
  INDEX `idx_testcase_id` (`testcase_id`),
  INDEX `idx_status` (`status`),
  CONSTRAINT `fk_submission_results_submission`
    FOREIGN KEY (`submission_id`)
    REFERENCES `videcode`.`submissions` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_submission_results_testcase`
    FOREIGN KEY (`testcase_id`)
    REFERENCES `videcode`.`testcases` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- -----------------------------------------------------
-- Table `videcode`.`submission_tokens`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `videcode`.`submission_tokens` (
  `token` CHAR(36) NOT NULL,
  `submission_id` CHAR(36) NOT NULL,
  `testcase_id` CHAR(36) NOT NULL,
  `status` ENUM('pending', 'processing', 'completed') NOT NULL DEFAULT 'pending',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `expires_at` TIMESTAMP NOT NULL,
  PRIMARY KEY (`token`),
  INDEX `idx_submission_id` (`submission_id`),
  INDEX `idx_testcase_id` (`testcase_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_expires_at` (`expires_at`),
  CONSTRAINT `fk_submission_tokens_submission`
    FOREIGN KEY (`submission_id`)
    REFERENCES `videcode`.`submissions` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_submission_tokens_testcase`
    FOREIGN KEY (`testcase_id`)
    REFERENCES `videcode`.`testcases` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- -----------------------------------------------------
-- Insert default programming languages
-- -----------------------------------------------------
INSERT INTO `videcode`.`programming_languages` (`name`, `version`, `file_extension`, `is_active`) VALUES
('C++', '17', '.cpp', 1),
('Java', '11', '.java', 1),
('Python', '3.9', '.py', 1),
('JavaScript', 'ES6', '.js', 1),
('C', '11', '.c', 1),
('Go', '1.19', '.go', 1),
('Rust', '1.65', '.rs', 1);

-------------------------------------------------------------------------
use videcode;
ALTER TABLE users DROP COLUMN role;
ALTER TABLE users DROP COLUMN round_qualified;
ALTER TABLE users DROP COLUMN score;
ALTER TABLE users DROP COLUMN reg_no;

ALTER TABLE `videcode`.`interviews`
MODIFY COLUMN `scheduled_date` DATETIME NULL;

-- Reset SQL modes and checks
SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;