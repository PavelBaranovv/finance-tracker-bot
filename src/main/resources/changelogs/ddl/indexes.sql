-- liquibase formatted sql
-- changeset pavel:create-indexes

CREATE INDEX idx_users_username ON users(username);

CREATE INDEX idx_add_purchase_states_user_id ON add_purchase_states(user_id);
CREATE INDEX idx_add_purchase_states_step ON add_purchase_states(step);

CREATE INDEX idx_exchange_rates_date ON exchange_rates(date);

CREATE INDEX idx_currency_rate_currency_id ON currency_rate(currency_id);

CREATE INDEX idx_purchases_user_id ON purchases(user_id);
