ALTER TABLE payments MODIFY COLUMN order_id BIGINT NULL;

SET @shipment_unique_index = (
    SELECT INDEX_NAME
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'payments'
      AND COLUMN_NAME = 'shipment_id'
      AND NON_UNIQUE = 0
    LIMIT 1
);

SET @shipment_lookup_index_exists = (
    SELECT 1
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'payments'
      AND COLUMN_NAME = 'shipment_id'
      AND NON_UNIQUE = 1
    LIMIT 1
);

SET @drop_shipment_unique_index = IF(
    @shipment_unique_index IS NULL,
    'SELECT 1',
    IF(
        @shipment_lookup_index_exists IS NULL,
        CONCAT(
            'ALTER TABLE payments DROP INDEX `',
            @shipment_unique_index,
            '`, ADD INDEX ix_payments_shipment_id (shipment_id)'
        ),
        CONCAT('ALTER TABLE payments DROP INDEX `', @shipment_unique_index, '`')
    )
);

PREPARE drop_shipment_unique_index FROM @drop_shipment_unique_index;
EXECUTE drop_shipment_unique_index;
DEALLOCATE PREPARE drop_shipment_unique_index;
