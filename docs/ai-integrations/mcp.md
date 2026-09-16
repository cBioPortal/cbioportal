# Model Context Protocol (MCP)

This document provides technical information about the Model Context Protocol (MCP) servers and architecture that power AI integrations with cBioPortal.

> **Note:** The cBioPortal MCP servers power the live cBioPortalChat interface and remain in active development, so their interfaces may still change.

## What is MCP?

The Model Context Protocol (MCP) is a standard protocol that enables AI assistants to interact with external data sources and tools. MCP servers act as bridges between AI models (like Claude) and databases, APIs, or other services, allowing the AI to access and query data in a structured way.

For details on how MCP powers the cBioPortal chat interface, see the [Chat Interface documentation](chat-interface.md).

## Available MCP Servers

### cBioPortal MCP Servers

The live **cBioPortalChat** bot is a single agent that uses **both** of the cBioPortal MCP servers below at the same time — it is one bot with two MCP servers:

- **`cbioportal-mcp`** powers the database-query capability (referred to as the "database MCP" in the [Chat Interface documentation](chat-interface.md)).
- **`cbioportal-navigator`** powers the link-generation capability (the "navigator MCP").

The agent decides which of the two to call for each turn. Both servers are developed by the cBioPortal team and remain in active development.

#### cbioportal-mcp

A specialized MCP server that wraps the ClickHouse database connection with cBioPortal-specific knowledge.

**Repository**: [https://github.com/cBioPortal/cbioportal-mcp](https://github.com/cBioPortal/cbioportal-mcp)

**Hosted endpoint**: A hosted instance backed by the cBioPortal database is available at `https://mcp.cbioportal.org/db/mcp` (streamable HTTP). Point an MCP-compatible client at this URL to query studies, samples, mutations, and clinical attributes in natural language. The endpoint requires authentication via Google sign-in (the only provider supported at the moment). MCP clients that support OAuth are prompted to sign in and register automatically on first connect.

The demo below adds the hosted endpoint as a custom connector in an MCP client (Claude) and asks it which studies are available:

<video controls muted playsinline style="max-width: 100%; height: auto;">
  <source src="https://github.com/user-attachments/assets/abd9fca3-c777-48c1-8a67-8efaed8d6b0a" type="video/mp4">
  Your browser does not support embedded video. <a href="https://github.com/user-attachments/assets/abd9fca3-c777-48c1-8a67-8efaed8d6b0a">Watch the demo</a>.
</video>

If you'd rather not connect your own client, you can try the same database MCP through the hosted [cBioPortalChat](chat-interface.md) interface at [https://chat.cbioportal.org](https://chat.cbioportal.org).

**Key Features**:
- Wraps the `mcp-clickhouse` server for ClickHouse database connectivity
- Includes cBioPortal-specific system prompts that teach the AI about cancer genomics data structures
- Enables natural language queries against cBioPortal's ClickHouse database
- Supports multiple deployment options (development mode, Docker)
- Includes MCP Inspector for debugging and monitoring

**How it works**: The server acts as an intermediary that combines ClickHouse database access with domain-specific instructions, allowing researchers and clinicians to query complex genomic datasets through conversational interfaces without writing SQL directly.

**Backing data store**: The server queries the ClickHouse database for AI agents. It has direct access to the same database tables used by the cBioPortal web application. See the [ClickHouse Setup Guide](/deployment/clickhouse/README.md) to set one up.

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

## Privacy

The hosted `cbioportal-mcp` endpoint at `https://mcp.cbioportal.org/db/mcp/` is operated by the cBioPortal team. When you connect an MCP client to it, we collect and store the following:

- **Your account identifier** — the OAuth flow authenticates you via Google; we store your Google account email and stable subject ID on every tool call so we can attribute traffic and rate-limit.
- **Tool call metadata** — per request we record the tool name, arguments, response status, latency, and the MCP client that made the call (Claude Desktop / claude.ai / Claude Code / etc.). This lands in Datadog for operational monitoring and debugging.
- **Database query text** — every ClickHouse query the server runs against cBioPortal's data warehouse is logged in ClickHouse's own `system.query_log` (query text, duration, rows read; no result data).

Unlike the [chat interface](chat-interface.md), the MCP server **does not see or store the conversation you're having with your AI client**. Your prompts and the model's responses are exchanged directly between your client (Claude Desktop, claude.ai, etc.) and its own model backend; the cBioPortal MCP server only receives the tool calls the model chooses to make. We can see *what tool was called with what arguments* — for example, the SQL a client asked us to run — but not the natural-language question that triggered it or the interpretation the model returned to you.

Data flow to third parties:

- **Google** — used only for sign-in (OAuth). Your Google account email + subject ID reach our server as part of the OAuth token.
- **Amazon Bedrock, Anthropic, OpenAI, etc.** — the MCP server does not talk to any AI model itself. Whatever model your client uses is between you and that provider; consult its privacy policy separately.
- **Datadog** — tool call telemetry (tool name, args, user email, latency, status) is stored in our Datadog organization for operational monitoring.
- **ClickHouse Cloud** — the underlying database provider stores query logs as part of standard operation.

We do **not** sell your data or share it with parties beyond the infrastructure providers above. Contact us via the [Google Group](https://groups.google.com/g/cbioportal) if you'd like your tool-call history purged or your Google account's authorization revoked.

Do not paste protected health information (PHI), personally identifying information about patients, or other sensitive data into the queries you ask your MCP client to run against this endpoint. The hosted MCP is a research tool for exploring published cancer-genomics datasets.

## Building Your Own MCP Integration

To build an MCP integration with cBioPortal:

1. **Contact the community** via [Google Group](https://groups.google.com/g/cbioportal) or [Slack](https://slack.cbioportal.org) to discuss your approach
2. **Access data** through:
   - [REST API](../web-API-and-Clients.md) - Programmatic access
   - [DataHub](https://github.com/cBioPortal/datahub) - Bulk data downloads
   - ClickHouse database - Direct queries (not publicly accessible; contact us to discuss options). For details on setting up ClickHouse for your own MCP deployment, see the [ClickHouse Setup Guide](/deployment/clickhouse/README.md).

## Resources

- [Model Context Protocol Specification](https://modelcontextprotocol.io/)
- [cbioportal-mcp GitHub Repository](https://github.com/cBioPortal/cbioportal-mcp)
- [BioMCP Documentation](https://biomcp.org/)
- [cBioPortal API Documentation](../web-API-and-Clients.md)
- [LibreChat Documentation](https://github.com/danny-avila/LibreChat)
