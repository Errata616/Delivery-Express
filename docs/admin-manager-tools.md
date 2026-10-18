# Admin and manager API tools

All routes below require authentication. Admin and manager shipment operations and
dashboard statistics are available to both roles; account administration is
restricted to administrators.

## User administration (ADMIN only)

- `GET /api/admin/users` lists user profiles without password fields.
- `GET /api/admin/users/{id}` retrieves one profile.
- `POST /api/admin/users` creates a `SHIPPER` or `MANAGER` account. See the
  `AdminCreateUserRequest` fields in the API implementation.
- `PUT /api/admin/users/{id}/role` changes a staff member's role:

  ```json
  { "role": "MANAGER" }
  ```

- `PATCH /api/admin/users/{id}/enabled` activates or deactivates an account:

  ```json
  { "enabled": false }
  ```

Role assignment is limited to `SHIPPER` and `MANAGER`; administrator accounts
cannot be changed or disabled by these endpoints. An administrator cannot disable
their own account. Deactivation and role changes revoke the account's refresh
tokens, and disabled users' existing access tokens are rejected.

## Shipment operations (ADMIN or MANAGER)

- `GET /api/admin/shipments/unassigned` lists unassigned shipments.
- `GET /api/admin/shipments/shippers` lists shipper profiles without password fields.
- `GET /api/admin/shipments/{shipmentId}` retrieves a shipment.
- `PUT /api/admin/shipments/{shipmentId}/assign/{shipperId}` assigns a shipper.

The shared shipment API also permits admins and managers to assign shippers and
list unassigned shipments.

## Dashboard statistics (ADMIN or MANAGER)

- `GET /api/statistics` returns dashboard totals and status/revenue summaries.
