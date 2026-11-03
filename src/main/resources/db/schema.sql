
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(10) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_category_type
        CHECK (type IN ('INCOME', 'EXPENSE')),
    CONSTRAINT uq_category_name_type UNIQUE (name, type)
);

CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_id BIGINT NULL,
    type VARCHAR(10) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    description VARCHAR(500),
    transaction_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_transaction_type
        CHECK (type IN ('INCOME', 'EXPENSE')),
    CONSTRAINT chk_transaction_amount
        CHECK (amount > 0),
    CONSTRAINT fk_transaction_category
        FOREIGN KEY (category_id) REFERENCES categories(id)
        ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS budgets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_id BIGINT NOT NULL,
    budget_month DATE NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_budget_amount CHECK (amount > 0),
    CONSTRAINT uq_budget_category_month UNIQUE (category_id, budget_month),
    CONSTRAINT fk_budget_category
        FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE TABLE IF NOT EXISTS recurring_payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_id BIGINT NULL,
    title VARCHAR(150) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    frequency VARCHAR(20) NOT NULL,
    next_due_date DATE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_recurring_amount CHECK (amount > 0),
    CONSTRAINT chk_recurring_frequency
        CHECK (frequency IN ('DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY')),
    CONSTRAINT fk_recurring_category
        FOREIGN KEY (category_id) REFERENCES categories(id)
        ON DELETE SET NULL
);
