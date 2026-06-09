# Security
We are committed to maintaining a secure and reliable platform by integrating proactive vulnerability detection and mitigation strategies into our development and release workflows. Here's  how we address security concerns at each stage of our development process.

## Current Vulnerability Status
We provide a [status badge](https://github.com/cBioPortal/cbioportal?tab=readme-ov-file#cbioportal) that displays the current vulnerability status of the cBioPortal application. Additionally, we maintain a Software Bill of Materials (SBOM) integrated with our Github Dependabot Security system.

## Vulnerability Detection in Pull Requests
We utilize **SonarCloud** to ensure the integrity and security of our codebase. SonarCloud is configured to automatically analyze all pull requests and merged code. This enables us to:
- Detect security vulnerabilities, bugs, and code smells during the review process.
- Generate detailed reports that identify specific issues, such as potential injections, insecure code patterns, or other security risks.
- Enforce quality gates that prevent merging unsafe code into the main branch.
The results of the analysis are presented as part of the Github pull request checks, allowing contributors and maintainers to address issues early in the development lifecycle.

## Container Image Security with Docker Scout
As part of our release process, we integrate **Docker Scout** to ensure the security of container images published to our public DockerHub repository. Key steps include:
- **Automated Scanning:** Every time a Docker image is built and pushed, Docker scout performs an in-depth analysis of all base layers and dependencies.
- **Detailed Reports:** Vulnerabilities are categorized by severity, and actionable remediation steps are provided. This ensures maintainers can quickly patch or replace vulnerable components.
- **Image Maintenance:** We montior and rebuild images when new vulnerabilities are detected in upstream dependencies, ensuring our images remain secure.
Docker Scout is a critical step in the release pipeline, ensuring that the container images used by our users are secure and free from known vulnerabilities.

## Continuous Monitoring and Updates
SonarCloud and Docker Scout work seamlessly within our development and release workflows:
- **SonarCloud** safeguards the integrity of the codebase during pull requests and merges.
- **Docker Scout** validates the security of container images as part of our weekly release cycle.
This dual-layered approach ensures that vulnerabilities are addressed both at the source code and container levels, offering comprehensive security coverage for the cBioPortal platform.

## Feedback and Contributions
We encourage the community to provide feedback and suggestions to enhance our security processes. If you have any questions or ideas, please contact us or submit an issue on our [Github repository](https://github.com/cBioPortal/cbioportal/security/policy).
