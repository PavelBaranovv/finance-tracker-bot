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