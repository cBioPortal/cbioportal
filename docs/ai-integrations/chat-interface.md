# Chat Interface

We're implementing an AI-powered chat interface that allows users to ask questions about cBioPortal data in plain English. Please reach out if you'd like to test it on cbioportal@googlegroups.com.

> **Note:** This is a prototype and work in progress. Features and functionality are actively being developed and improved.

## What is the Chat Interface?

The cBioPortal chat interface is an experimental platform with multiple AI agents currently in development. These early prototypes use Claude, an LLM developed by Anthropic, and are designed for different use cases:

- **cBioDBAgent** - Query the cBioPortal database for information about studies, patients, samples, and treatments
- **cBioDocsAgent** - Get help using and deploying cBioPortal by referencing documentation
- **cBioNavigator** - Navigate the cBioPortal web interface

We are actively exploring various approaches to make cBioPortal more accessible through natural language interactions. These agents are early prototypes and their capabilities will evolve as we continue development.

## Agent-Specific Features

### cBioDBAgent

Queries the cBioPortal database directly to answer questions about data:

- **Natural Language Queries**: Ask questions in plain English without needing to know the technical details of the database structure
- **Study Information**: Get quick answers about the number of studies, patients, and samples in cBioPortal
- **Sample Analysis**: Query specific details about samples, including sample types and patient demographics
- **Treatment Data**: Explore treatment information across different studies
- **Data Exploration**: Discover insights from the extensive cancer genomics datasets available in cBioPortal

**Example Queries:**
- General Stats
   - [How many studies are in cBioPortal?](https://chat.cbioportal.org/c/new?q=How%20many%20studies%20are%20there%20in%20cBioPortal?&submit=true&spec=cBioDBAgent)
- Study-Specific Questions
    - [How many patients and samples are in the MSK-CHORD Study?](https://chat.cbioportal.org/c/new?q=How%20many%20patients%20and%20samples%20are%20in%20the%20MSK%20CHORD%20Study?&submit=true&spec=cBioDBAgent)
    - [What treatment did most patients receive in the MSK-CHORD Study?](https://chat.cbioportal.org/c/new?q=What%20treatment%20did%20most%20patients%20recieve%20in%20the%20MSK%20CHORD%20study?&submit=true&spec=cBioDBAgent)

### cBioDocsAgent

Helps you understand how to use and deploy cBioPortal by referencing documentation:

- Search and retrieve information from cBioPortal documentation
- Get guidance on deployment, configuration, and usage
- Find answers to technical questions about cBioPortal

### cBioNavigator

Assists with navigating the cBioPortal web interface:

- Help understanding how to use different features in the web interface
- Guidance on navigating through studies and visualizations
- Support for exploring cBioPortal's web-based tools

## Tips for Using the Chat Interface

- **Choose the right agent**: Select the agent that best matches your need - data queries (cBioDBAgent), documentation (cBioDocsAgent), or web navigation (cBioNavigator)
- **Be specific**: Include study names or specific data types when possible
- **Ask follow-up questions**: The chat interface maintains context, so you can ask related questions in sequence
- **Explore different angles**: Try rephrasing questions or asking for different perspectives on the data
- **Learn about the database structure** (cBioDBAgent): You can ask the LLM to explain the database schema and available fields - this can help you formulate more effective questions

## Getting Started

1. Reach out to cbioportal@googlegroups.com to get access to the chat interface
2. Type your question in the chat input
3. Review the AI-generated response
4. Ask follow-up questions to dive deeper into the data

## Feedback and Support

The AI chat interface is actively being developed and improved. Your feedback helps us make it better:

- **Use the thumbs up/down buttons**: Each chat response has thumbs up and thumbs down buttons. Please use them to rate the quality and accuracy of the responses - this feedback directly helps us improve the AI's answers.
- **Report issues or suggestions**: If you encounter any issues or have suggestions for improvement, please reach out through the [cBioPortal Google Group](https://groups.google.com/g/cbioportal).

## Technical Details

### Architecture

All agents share a common technical foundation:

- **User Interface**: Built with [LibreChat](https://github.com/danny-avila/LibreChat), an open-source chat interface
- **AI Model**: Uses [Claude](https://www.anthropic.com/claude) (provided via Amazon Bedrock), Anthropic's large language model

Each agent has specialized components depending on its function:

**cBioDBAgent:**
- **MCP Layer**: Model Context Protocol (MCP) servers provide the connection between Claude and cBioPortal data
- **Database**: Queries are executed against cBioPortal's [ClickHouse](https://clickhouse.com/) database

**cBioDocsAgent:**
- **Documentation Access**: References cBioPortal documentation to answer questions

**cBioNavigator:**
- **Web Interface Tools**: Uses MCP tools to interact with the cBioPortal website

For more information about the MCP servers and how to build your own integrations, see the [Model Context Protocol documentation](mcp.md).
