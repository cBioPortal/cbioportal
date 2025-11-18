# Chat Interface

We're implementing an AI-powered chat interface that allows users to ask questions about cBioPortal data in plain English. Please reach out if you'd like to test it on cbioportal@googlegroups.com.

> **Note:** This is a prototype and work in progress. Features and functionality are actively being developed and improved.

## What is the Chat Interface?

The cBioPortal chat interface uses Claude, an LLM developed by Anthropic, to help you explore cancer genomics data through conversational queries. We are exploring various approaches, including natural language to database queries, and website navigation. Instead of navigating through multiple pages or writing complex queries, you can simply ask questions about studies, patients, samples, and treatments in natural language.

## Key Features

- **Natural Language Queries**: Ask questions in plain English without needing to know the technical details of the database structure
- **Study Information**: Get quick answers about the number of studies, patients, and samples in cBioPortal
- **Sample Analysis**: Query specific details about samples, including sample types and patient demographics
- **Treatment Data**: Explore treatment information across different studies
- **Data Exploration**: Discover insights from the extensive cancer genomics datasets available in cBioPortal

## Example Queries

Here are some example questions you can ask the chat interface:

### General Portal Information
- "How many studies are in cBioPortal?"

### Study-Specific Queries
- "How many patients and samples are in the MSK-CHORD Study?"
- "How many primary samples are in the MSK-CHORD Study?"
- "What treatment did most patients receive in the MSK-CHORD Study?"

## Tips for Using the Chat Interface

- **Be specific**: Include study names or specific data types when possible
- **Ask follow-up questions**: The chat interface maintains context, so you can ask related questions in sequence
- **Explore different angles**: Try rephrasing questions or asking for different perspectives on the data
- **Learn about the database structure**: You can ask the LLM to explain the database schema and available fields - this can help you formulate more effective questions

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

The cBioPortal chat interface is built on the following technical stack:

- **User Interface**: Built with [LibreChat](https://github.com/danny-avila/LibreChat), an open-source chat interface
- **AI Model**: Uses [Claude](https://www.anthropic.com/claude), Anthropic's large language model
- **MCP Layer**: Model Context Protocol (MCP) servers provide the connection between Claude and cBioPortal data
- **Database**: Queries are executed against cBioPortal's [ClickHouse](https://clickhouse.com/) database

### How It Works

When you ask a question in the chat interface:

1. Your natural language question is sent to Claude (Provided via Amazon Bedrock)
2. Claude uses MCP servers to understand the database schema and formulate appropriate queries
3. The MCP server translates Claude's intent into ClickHouse SQL queries
4. Query results are returned to Claude, which formats them into a natural language response
5. You receive the answer along with relevant data

For more information about the MCP servers and how to build your own integrations, see the [Model Context Protocol documentation](mcp.md).
