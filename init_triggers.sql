CREATE OR REPLACE FUNCTION check_id_not_null()
RETURNS TRIGGER AS $$  
BEGIN
  IF NEW.id IS NULL THEN
      RAISE EXCEPTION 'ID cannot be NULL';
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trig_check_user_id
BEFORE INSERT OR UPDATE ON users
FOR EACH ROW EXECUTE FUNCTION check_id_not_null();

CREATE TRIGGER trig_check_purchase_id
BEFORE INSERT OR UPDATE ON purchases
FOR EACH ROW EXECUTE FUNCTION check_id_not_null();

CREATE OR REPLACE FUNCTION check_purchase_required_fields()
RETURNS TRIGGER AS $$  
BEGIN
  IF NEW.name IS NULL OR NEW.quantity IS NULL OR NEW.amount IS NULL OR NEW.currency IS NULL OR NEW.purchase_date IS NULL THEN
      RAISE EXCEPTION 'Required fields (name, quantity, amount, currency, purchase_date) cannot be NULL';
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trig_check_purchase_fields
BEFORE INSERT OR UPDATE ON purchases
FOR EACH ROW EXECUTE FUNCTION check_purchase_required_fields();