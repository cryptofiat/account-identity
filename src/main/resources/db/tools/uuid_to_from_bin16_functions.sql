-- useful for db debugging. AuthIdentifiers are UUID format, id db stored as optimal binary(16) instead of char(36). Functions below can used for conversions in custom sql queries.
DELIMITER $$
CREATE FUNCTION uuid_from_bin16(b BINARY(16))
RETURNS CHAR(36) DETERMINISTIC
BEGIN
  DECLARE hex CHAR(32);
  SET hex = HEX(b);
  RETURN LOWER(CONCAT(LEFT(hex, 8), '-', MID(hex, 9,4), '-', MID(hex, 13,4), '-', MID(hex, 17,4), '-', RIGHT(hex, 12)));
END;
$$

CREATE FUNCTION uuid_to_bin16(s CHAR(36))
RETURNS BINARY(16) DETERMINISTIC
RETURN UNHEX(CONCAT(LEFT(s, 8), MID(s, 10, 4), MID(s, 15, 4), MID(s, 20, 4), RIGHT(s, 12)))
$$
DELIMITER ;
