# Instructions for AI coding agents

## Project and scope

Read README.md, CONTRIBUTING.md, docs/02-business-rules.md and the active ticket before editing.
This is a solo Java/Spring Boot rental management project. Implement one bounded ticket at a time.
User instructions take priority. Make routine reversible decisions within the authorized ticket; record assumptions.
Do not create extra features, dependencies, architecture layers or permission gates without a concrete need.

## Work sequence

1. Inspect the current branch, working tree, relevant code and tests. Preserve unrelated changes.
2. Summarize the requirement, acceptance criteria, files affected and verification plan.
3. Implement the smallest complete change, including migrations/API docs when needed.
4. Run the relevant real checks. Once CI exists, satisfy required checks. Do not claim checks ran when they did not.
5. Review the diff for correctness, authorization, transactions, secrets and accidental scope expansion.
6. Report: changed behavior, files, commands and results, remaining limitations, proposed commit subject.
7. For teaching, explain the request flow and one key Java concept used in this ticket.

## Architecture and correctness

Use a modular monolith. See docs/03-architecture.md for the dependency direction.
Keep Domain free of Spring/JPA dependencies. Use DTOs at API boundaries, explicit mapping and constructor injection.
Infrastructure implements application ports. All state-changing use cases have a clear transaction boundary.
Use BigDecimal for money; define unit and rounding explicitly. Do not use float/double for billing.
Enforce authorization in the backend against the authenticated identity and the target rental agreement.
Do not accept owner identity, payment status, privileged role or authoritative totals blindly from clients.
Validate ranges; use database constraints and concurrency control for duplicate/overlapping operations.
Do not use ddl-auto=update in shared environments. Version schema changes as migrations.
Do not delete billing/payment history to make tests pass. Do not silently change an issued invoice.

## Tests and evidence

Prioritize behavior: cross-tenant access denial, overlapping leases, duplicate bills, invalid meter readings,
rate snapshots, partial payments, concurrent requests and transaction rollback where relevant.
Use PostgreSQL integration checks for rules dependent on PostgreSQL; an in-memory substitute is not proof.
Fix causes; do not disable tests, authorization, lint rules or quality gates to obtain a green build.
Documentation-only changes need link/content review; do not invent meaningless unit tests.
Mock data and integrations must be explicitly labeled. Never report a mock as a working bank/AI integration.

## Git and external actions

Follow CONTRIBUTING.md. Inspect staged changes before committing; stage only intended files.
Never invent author information or use somebody else's email. Never fabricate commit history, approvals or test results.
Use permissions already established by the user. Do not push to an unknown remote or guess repository visibility.
Do not force-push, rewrite published history or delete branches/data without explicit task authorization.
Do not commit secrets, real tenant personal data, real contracts, build artifacts or local environment files.
AI review is an additional check; do not present it as an independent human approval.
