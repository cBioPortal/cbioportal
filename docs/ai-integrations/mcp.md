# Model Context Protocol (MCP)

This document provides technical information about the Model Context Protocol (MCP) servers and architecture that power AI integrations with cBioPortal.

> **Note:** All these are prototypes and work in progress. Features and functionality are actively being developed and improved.

## What is MCP?

The Model Context Protocol (MCP) is a standard protocol that enables AI assistants to interact with external data sources and tools. MCP servers act as bridges between AI models (like Claude) and databases, APIs, or other services, allowing the AI to access and query data in a structured way.

For details on how MCP powers the cBioPortal chat interface, see the [Chat Interface documentation](chat-interface.md).

## Available MCP Servers

### cBioPortal MCP Servers in Development

These MCP servers are early prototypes developed by the cBioPortal team and are in active development.

#### cbioportal-mcp

A specialized MCP server that wraps the ClickHouse database connection with cBioPortal-specific knowledge.

**Repository**: [https://github.com/cBioPortal/cbioportal-mcp](https://github.com/cBioPortal/cbioportal-mcp)

**Key Features**:
- Wraps the `mcp-clickhouse` server for ClickHouse database connectivity
- Includes cBioPortal-specific system prompts that teach the AI about cancer genomics data structures
- Enables natural language queries against cBioPortal's ClickHouse database
- Supports multiple deployment options (development mode, Docker)
- Includes MCP Inspector for debugging and monitoring

**How it works**: The server acts as an intermediary that combines ClickHouse database access with domain-specific instructions, allowing researchers and clinicians to query complex genomic datasets through conversational interfaces without writing SQL directly.

**Configuration**: Uses environment variables for ClickHouse connection details and supports different transport protocols (stdio, HTTP, SSE).

#### cbioportal-navigator

An MCP server that enables AI agents to navigate and interact with the cBioPortal web interface.

**Repository**: [https://github.com/fuzhaoyuan/cbioportal-navigator](https://github.com/fuzhaoyuan/cbioportal-navigator)

**Key Features**:
- Provides tools for navigating the cBioPortal website
- Enables AI agents to interact with the web interface programmatically
- Supports automated exploration of cBioPortal's web-based features

### Community MCP Integrations

These integrations are maintained by the community and third-party developers.

#### BioMCP - cBioPortal Integration

An MCP integration that enhances biomedical literature searches with cBioPortal genomic data. When searching for articles with gene parameters, BioMCP automatically queries cBioPortal alongside PubMed searches, enriching results with mutation frequency and distribution data across cancer studies.

**Maintainer**: [BioMCP community](https://biomcp.org/)

**Website**: [https://biomcp.org/backend-services-reference/03-cbioportal/](https://biomcp.org/backend-services-reference/03-cbioportal/)

For more details on features and technical implementation, see the [official BioMCP documentation](https://biomcp.org/backend-services-reference/03-cbioportal/).

## Building Your Own MCP Integration

To build an MCP integration with cBioPortal:

1. **Contact the community** via [Google Group](https://groups.google.com/g/cbioportal) or [Slack](https://slack.cbioportal.org) to discuss your approach
2. **Access data** through:
   - [REST API](../web-API-and-Clients.md) - Programmatic access
   - [DataHub](https://github.com/cBioPortal/datahub) - Bulk data downloads
   - ClickHouse database - Direct queries (not publicly accessible; contact us to discuss options)

## Resources

- [Model Context Protocol Specification](https://modelcontextprotocol.io/)
- [cbioportal-mcp GitHub Repository](https://github.com/cBioPortal/cbioportal-mcp)
- [BioMCP Documentation](https://biomcp.org/)
- [cBioPortal API Documentation](../web-API-and-Clients.md)
- [LibreChat Documentation](https://github.com/danny-avila/LibreChat)
