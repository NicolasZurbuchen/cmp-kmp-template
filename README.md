# cmp-kmp-template

## Local Setup

This project uses **Husky** and **Commitlint** to enforce conventional commit messages.

### Prerequisites

- [Node.js](https://nodejs.org/) (includes npm)

### Initial Setup

After forking or cloning the project, run the following command in the root directory to install the commit linting tools and initialize the Git hooks:

```bash
npm install
```

### Commit Message Convention

We follow the [Conventional Commits](https://www.conventionalcommits.org/) specification. 

**Format:** `<type>(<scope>): <description>`

- **Types:** `feat`, `fix`, `refactor`, `build`, `chore`, `ci`, `docs`, `perf`, `style`, `test`, `revert`.
- **Scopes:** `sync`, `network`, `database`, `di`, `navigation`, `theme`, `common`, `gradle`, `deps`, `feature-a`, `feature-b`.
- **Note:** A scope is **required** for types: `feat`, `fix`, `refactor`, and `build`.

> [!TIP]
> You can customize the allowed scopes to match your project's features in `commitlint.config.js`.

Example of a valid commit:
`feat(common): add new utility function`
