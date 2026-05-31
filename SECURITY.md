# Security Policy

TimeTask Pro V2 is an offline-first Android productivity app. Security and privacy reports are taken seriously, especially issues involving local task data, exported backups, notification behavior, AI context sharing, or credential handling.

## Supported Versions

The project is currently in active alpha. Until the first public release is available, security fixes target the default branch.

| Version | Supported |
| --- | --- |
| Unreleased / main | Yes |
| v0.x alpha releases | Best effort |

## Reporting a Vulnerability

Please do not open a public issue for sensitive vulnerabilities.

Use GitHub's private vulnerability reporting if it is enabled for the repository. If it is not enabled yet, open a minimal public issue asking for a private security contact without disclosing exploit details.

Include:

- affected version or commit;
- Android version and device/emulator;
- clear reproduction steps;
- impact and data exposure risk;
- logs, screenshots, or proof of concept if safe to share privately.

## Out of Scope

These are usually not treated as security vulnerabilities:

- issues requiring a rooted or fully compromised device;
- local debug builds intentionally installed by the user;
- social engineering outside the app;
- missing features from the AI roadmap that are not implemented yet.

## Secrets and API Keys

Do not commit:

- OpenAI API keys;
- keystores;
- signing credentials;
- `.env` files;
- `local.properties`;
- private tokens;
- APKs containing private credentials.

Future OpenAI-backed features should load credentials only through local, user-controlled configuration and should never store secrets in the public repository.

## AI and Privacy

AI features should follow these rules:

- send the minimum useful context;
- require explicit user action before sharing task data;
- return structured output;
- require confirmation before writing AI-generated changes;
- provide local fallback behavior where practical.

