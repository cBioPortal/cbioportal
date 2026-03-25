# 🛡️ Study Permission Evaluator

This class is an implementation of the **Spring Security `PermissionEvaluator`** interface. Its purpose is to enforce fine-grained access control across the application's data model, ensuring that authenticated users have the required `AccessLevel` to interact with specific cancer research entities.

Access control is centrally determined at the **`CancerStudy`** level. All other related entities (profiles, lists, patients) inherit their access policy from their associated study.

***

## Core Functionality

The evaluator implements two primary permission checking methods:

### 1. Object-Based Permission Check

| Method | `hasPermission(Authentication, Object targetDomainObject, Object permission)` |
| :--- | :--- |
| **Purpose** | Checks permission on a **single, loaded domain object instance** (e.g., a `MolecularProfile` object). |
| **Process** | It uses `extractCancerStudy()` to resolve the target object back to its parent `CancerStudy` and delegates the final access check to a business logic method (`hasAccessToCancerStudy`). |

### 2. ID-Based Permission Check

| Method | `hasPermission(Authentication, Serializable targetId, String targetType, Object permission)` |
| :--- | :--- |
| **Purpose** | Checks permission on resources identified by an **ID, a collection of IDs, or a filter object**. |
| **Process** | It resolves the IDs/filter into a collection of unique `CancerStudy` objects. It then enforces an **all-or-nothing** policy: access is granted only if the user has permission to **every single** associated `CancerStudy`. |

***

## Supported Target Types

The evaluator is designed to handle access checks for objects and identifiers spanning the core data model:

| Target Object/ID Type | Description |
| :--- | :--- |
| `CancerStudy` | Direct object or ID. The root entity for access control. |
| `MolecularProfile` | Object or ID. Access is determined by its parent study. |
| `SampleList` | Object or ID. Access is determined by its parent study. |
| `Patient` | Direct object. Access is determined by its parent study. |
| `Collection<...Ids>` | A bulk check on a collection of any supported ID type. |
| `*Filter` Objects | Various application-specific filter classes (e.g., `SampleFilter`, `StudyViewFilter`) that implicitly reference a set of `CancerStudy` IDs. |

***

## Dependencies

* **`Authentication`**: The standard Spring Security principal representing the logged-in user.
* **`cacheMapUtil`**: A utility used for high-performance retrieval of domain objects (like `CancerStudy`, `MolecularProfile`) by their string identifiers, crucial for efficient ID-based permission checks.
* **`AccessLevel`**: An external enumeration (cast from the `permission` object) defining the specific level of access requested (e.g., `READ`, `WRITE`).
