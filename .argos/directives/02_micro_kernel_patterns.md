Directive: Core Minimalism and Plugin Architecture.

Rules:

The kernel package must be agnostic of business logic. It handles routing and lifecycle only.

New capabilities must be implemented as DomainModules.

Communication between the Kernel and Modules must happen through defined Ports (Interfaces).

The UI layer must treat Vocal Input and Audio Output as Asynchronous Sidecars. The core application logic must remain functional even if the Vocal Service is detached or disabled.
