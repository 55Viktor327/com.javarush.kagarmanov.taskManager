CREATE TABLE IF NOT EXISTS users (
                                     id          BIGSERIAL PRIMARY KEY,
                                     username    VARCHAR(255) NOT NULL UNIQUE,
                                     email       VARCHAR(255) NOT NULL UNIQUE,
                                     password    VARCHAR(255) NOT NULL,
                                     role        VARCHAR(20) NOT NULL DEFAULT 'GUEST',
                                     deleted     BOOLEAN NOT NULL DEFAULT FALSE,
                                     deleted_at  TIMESTAMP,
                                     deleted_by  BIGINT
);

-- CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
-- CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
-- CREATE INDEX IF NOT EXISTS idx_users_deleted ON users(deleted);

CREATE TABLE IF NOT EXISTS tasks (
                                     id          BIGSERIAL PRIMARY KEY,
                                     title       VARCHAR(50) NOT NULL,
                                     description VARCHAR(255) NOT NULL,
                                     priority    VARCHAR(20),
                                     status      VARCHAR(20) NOT NULL,
                                     created_at  TIMESTAMP NOT NULL,
                                     deadline    TIMESTAMP NOT NULL,
                                     owner_id    BIGINT NOT NULL,
                                     deleted     BOOLEAN NOT NULL DEFAULT FALSE,
                                     deleted_at  TIMESTAMP,
                                     deleted_by  BIGINT,
                                     updated_at  TIMESTAMP,

                                     CONSTRAINT fk_task_owner
                                         FOREIGN KEY (owner_id)
                                             REFERENCES users(id)
                                             ON DELETE RESTRICT
);

-- CREATE INDEX IF NOT EXISTS idx_tasks_owner ON tasks(owner_id);
-- CREATE INDEX IF NOT EXISTS idx_tasks_status ON tasks(status);
-- CREATE INDEX IF NOT EXISTS idx_tasks_priority ON tasks(priority);
-- CREATE INDEX IF NOT EXISTS idx_tasks_deadline ON tasks(deadline);
-- CREATE INDEX IF NOT EXISTS idx_tasks_deleted ON tasks(deleted);

CREATE TABLE IF NOT EXISTS task_assignees (
                                              task_id     BIGINT NOT NULL,
                                              user_id     BIGINT NOT NULL,

                                              PRIMARY KEY (task_id, user_id),

                                              CONSTRAINT fk_assignee_task
                                                  FOREIGN KEY (task_id)
                                                      REFERENCES tasks(id)
                                                      ON DELETE CASCADE,

                                              CONSTRAINT fk_assignee_user
                                                  FOREIGN KEY (user_id)
                                                      REFERENCES users(id)
                                                      ON DELETE CASCADE
);
--
-- CREATE INDEX IF NOT EXISTS idx_task_assignees_task ON task_assignees(task_id);
-- CREATE INDEX IF NOT EXISTS idx_task_assignees_user ON task_assignees(user_id);
