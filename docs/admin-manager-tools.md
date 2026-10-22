# Admin and manager API tools

All routes below require authentication. Admin and manager shipment operations and
dashboard statistics are available to both roles; account administration is
restricted to administrators.

## User administration (ADMIN only)

- `GET /api/admin/users` lists user profiles without password fields.
- `GET /api/admin/users/{id}` retrieves one profile.
- `DELETE /api/admin/users/{id}` permanently deletes a non-admin account and
  returns `204 No Content`. Deleting a manager removes shipper assignments;
  deleting a shipper clears its historical assignment references and zones.
  Accounts with customer orders, shipments, reviews, shops, or active shipper
  shipments cannot be deleted, preserving business history.
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

- `PUT /api/admin/users/{shipperId}/manager` assigns a shipper to a manager. Set
  `managerId` to `null` to remove the assignment:

  ```json
  { "managerId": 12 }
  ```

Each shipper can belong to at most one manager. Managers can assign shipments
only to shippers assigned to their team. Manager-facing shipper lists, shipper
zone records and assignment suggestions are restricted to that manager's team;
managers cannot read or edit another team's shipper zones. Administrators can
manage all shippers and managers.
User profiles include `managerId` and `managerName` for assigned shippers.
Shipper profiles also include `available`, the shipper's self-managed status for
accepting new assignments. This is separate from `enabled`, which controls
whether the account can sign in.

Shippers can read and update their own availability:

- `GET /api/shipper/me/availability`
- `PATCH /api/shipper/me/availability` with `{ "available": false }`

Shippers who are not available are excluded from assignment suggestions and
cannot be assigned new shipments. Existing assignments are not changed.

Role assignment is limited to `SHIPPER` and `MANAGER`; administrator accounts
cannot be changed, disabled, or deleted by these endpoints. An administrator
cannot disable their own account. Deactivation and role changes revoke the
account's refresh tokens, and disabled users' existing access tokens are rejected.

## Shipment operations (ADMIN or MANAGER)

- `GET /api/admin/shipments/current` lists current shipments that are not
  delivered or refunded. Managers see unassigned shipments and current
  assignments belonging to their own shippers.
- `GET /api/admin/shipments/unassigned` lists unassigned shipments.
- `GET /api/admin/shipments/shippers` lists shipper profiles without password fields.
- `GET /api/admin/shipments/{shipmentId}` retrieves a shipment.
- `PUT /api/admin/shipments/{shipmentId}/assign/{shipperId}` assigns a shipper.

The shared shipment API also permits admins and managers to assign shippers and
list unassigned shipments.

## Dashboard statistics (ADMIN or MANAGER)

- `GET /api/statistics` returns dashboard totals and status/revenue summaries.
