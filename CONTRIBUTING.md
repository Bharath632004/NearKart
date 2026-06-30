# Contributing to NearKart

## Branch Strategy

```
main          → production-ready code
develop       → integration branch
feature/*     → new features (e.g. feature/auth-service)
hotfix/*      → urgent fixes
release/*     → release candidates
```

## Commit Convention

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
feat: add GPS-based nearby shops API
fix: resolve cart total calculation bug
chore: update dependencies
docs: add API documentation for order service
test: add unit tests for payment service
```

## Pull Request Guidelines
- Branch from `develop`
- Write unit tests for new features
- Update relevant README or docs
- Request review before merging
