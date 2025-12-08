CREATE OR REPLACE PROCEDURE insert_user(p_login VARCHAR(255), p_name VARCHAR(255))
LANGUAGE plpgsql
AS $$
BEGIN
	INSERT INTO users (login, name) VALUES (p_login, p_name);
END;
$$;

CREATE OR REPLACE PROCEDURE insert_purchase(p_user_id INTEGER, p_name VARCHAR(255), p_characteristics TEXT, p_quantity INTEGER, p_amount DECIMAL(10, 2), p_currency VARCHAR(3), p_purchase_date DATE)
LANGUAGE plpgsql
AS $$  
BEGIN
  INSERT INTO purchases (user_id, name, characteristics, quantity, amount, currency, purchase_date)
  VALUES (p_user_id, p_name, p_characteristics, p_quantity, p_amount, p_currency, p_purchase_date);
END;
$$;

CREATE OR REPLACE PROCEDURE insert_exchange_rate(p_rate_date DATE, p_rub DECIMAL(10, 4), p_eur DECIMAL(10, 4), p_cny DECIMAL(10, 4), p_kzt DECIMAL(10, 4), p_jpy DECIMAL(10, 4), p_chf DECIMAL(10, 4), p_gbp DECIMAL(10, 4), p_inr DECIMAL(10, 4), p_byn DECIMAL(10, 4))
LANGUAGE plpgsql
AS $$  
BEGIN
  INSERT INTO exchange_rates (rate_date, rub, eur, cny, kzt, jpy, chf, gbp, inr, byn)
  VALUES (p_rate_date, p_rub, p_eur, p_cny, p_kzt, p_jpy, p_chf, p_gbp, p_inr, p_byn);
END;
$$;

CREATE OR REPLACE PROCEDURE delete_user(p_login VARCHAR(255))
LANGUAGE plpgsql
AS $$  
BEGIN
  DELETE FROM users WHERE login = p_login;
END;
$$;

CREATE OR REPLACE PROCEDURE delete_purchase(p_id INTEGER)
LANGUAGE plpgsql
AS $$  
BEGIN
  DELETE FROM purchases WHERE id = p_id;
END;
$$;

CREATE OR REPLACE PROCEDURE update_user(p_login VARCHAR(255), p_new_name VARCHAR(255))
LANGUAGE plpgsql
AS $$  
BEGIN
  UPDATE users SET name = p_new_name WHERE login = p_login;
END;
$$;

CREATE OR REPLACE PROCEDURE update_purchase(p_id INTEGER, p_new_name VARCHAR(255), p_new_characteristics TEXT, p_new_quantity INTEGER, p_new_amount DECIMAL(10, 2), p_new_currency VARCHAR(3), p_new_purchase_date DATE)
LANGUAGE plpgsql
AS $$  
BEGIN
  UPDATE purchases SET 
      name = p_new_name, 
      characteristics = p_new_characteristics, 
      quantity = p_new_quantity, 
      amount = p_new_amount, 
      currency = p_new_currency, 
      purchase_date = p_new_purchase_date 
  WHERE id = p_id;
END;
$$;

CREATE OR REPLACE FUNCTION get_rate(p_curr VARCHAR(3), p_date DATE)
RETURNS DECIMAL(10, 4)
LANGUAGE sql
AS $$
    SELECT CASE LOWER(p_curr)
        WHEN 'usd' THEN usd
        WHEN 'rub' THEN rub
        WHEN 'eur' THEN eur
        WHEN 'cny' THEN cny
        WHEN 'kzt' THEN kzt
        WHEN 'jpy' THEN jpy
        WHEN 'chf' THEN chf
        WHEN 'gbp' THEN gbp
        WHEN 'inr' THEN inr
        WHEN 'byn' THEN byn
    END
    FROM exchange_rates
    WHERE rate_date = p_date;
$$;

CREATE OR REPLACE PROCEDURE get_monthly_stats(
    p_user_id INTEGER,
    p_year INTEGER,
    p_month INTEGER,
    p_target_curr VARCHAR(3),
    OUT p_result refcursor
)
LANGUAGE plpgsql
AS $$
DECLARE
    start_date DATE := make_date(p_year, p_month, 1);
    end_date DATE := start_date + INTERVAL '1 MONTH' - INTERVAL '1 DAY';
BEGIN
    OPEN p_result FOR
    WITH daily_purchases AS (
        SELECT
            extract(day from p.purchase_date)::INTEGER AS day,
            SUM(
                (p.amount / get_rate(p.currency, p.purchase_date)) * get_rate(p_target_curr, p.purchase_date)
            ) AS total
        FROM purchases p
        WHERE p.user_id = p_user_id
        AND p.purchase_date >= start_date
        AND p.purchase_date <= end_date
        GROUP BY p.purchase_date
    )
    SELECT
        gs.day,
        COALESCE(dp.total, 0::DECIMAL(10,2)) AS total_spent
    FROM generate_series(1, extract(day from end_date)::INTEGER) gs(day)
    LEFT JOIN daily_purchases dp ON gs.day = dp.day
    ORDER BY gs.day;
END;
$$;