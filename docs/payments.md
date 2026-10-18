# Simulated shipment payments

The payment API supports the standalone shipment booking flow. The customer must be
authenticated and own the shipment. The server takes the amount from the shipment's
shipping fee; request bodies cannot set the amount or mark a payment as paid.

## Create and check a payment

Create a payment with `POST /api/payments`:

```json
{
  "shipmentId": 123
}
```

The response is a `PENDING` payment in VND. Repeating the request while a payment is
pending returns that same attempt. A new attempt can be created after a previous one
has been cancelled or failed.

Use `GET /api/payments/{paymentId}` to check the status and
`POST /api/payments/{paymentId}/cancel` to cancel a pending payment. Payment details
are visible only to the shipment owner or an administrator.

## Simulate the payment callback

Set `PAYMENT_WEBHOOK_SECRET` in the application's environment to a random secret
with at least 32 UTF-8 bytes. Keep it out of source control. The webhook verifies
`x-signature` as a hexadecimal HMAC-SHA256 of the exact raw JSON request body.

Example body:

```json
{"paymentId":123,"status":"PAID","transactionCode":"SIM-123"}
```

Send this to `POST /api/payments/webhook` with the computed `x-signature`. The same
signature can be retried safely; already-paid callbacks are idempotent. Failed and
cancelled callbacks cannot change a paid payment. A standalone shipment stays in
`READY` after payment and cannot be assigned to a shipper until its payment is
`PAID`.

For local testing, sign the exact body string and submit it without reformatting:

```javascript
const crypto = require("node:crypto");
const body = '{"paymentId":123,"status":"PAID","transactionCode":"SIM-123"}';
const signature = crypto
  .createHmac("sha256", process.env.PAYMENT_WEBHOOK_SECRET)
  .update(body, "utf8")
  .digest("hex");
console.log(signature);
```

Then send `body` as the raw request body with `Content-Type: application/json` and
`x-signature: <printed-signature>`.

## Existing MySQL database

On an existing MySQL database, start the app once so Hibernate adds `shipment_id`,
then apply
[`V1__allow_shipment_payments.sql`](../src/main/resources/db/migration/mysql/V1__allow_shipment_payments.sql)
once before creating shipment payments. The migration relaxes the old
`payments.order_id` NOT NULL constraint and removes any unique index on `shipment_id`
so retries can be stored. Fresh databases created from the updated entity mappings
do not need this migration.
