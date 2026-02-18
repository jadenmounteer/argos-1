Directive: Core Minimalism and Plugin Architecture.

Rules:

The kernel package must be agnostic of business logic. It handles routing and lifecycle only.

New capabilities must be implemented as DomainModules.

Communication between the Kernel and Modules must happen through defined Ports (Interfaces).
