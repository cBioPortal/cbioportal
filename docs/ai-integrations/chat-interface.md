# Chat Interface

We're implementing an AI-powered chat interface that allows users to ask questions about cBioPortal data in plain English. We're looking for early test users to provide us with feedback and help guide future development. If that sounds like you, sign up [here](https://docs.google.com/forms/d/e/1FAIpQLSfQ53xWgzZRu5qMINOqZCfK_8StG7bjbtJ7WsQM9fZpe1bq3A/viewform).

> **Note:** This is a prototype and work in progress. Features and functionality are actively being developed and improved.

## What is the Chat Interface?

The cBioPortal chat interface is an experimental platform with multiple AI agents currently in development. These early prototypes use Claude, an LLM developed by Anthropic, and are designed for different use cases:

- **cBioDBAgent** - Query the cBioPortal database for information about studies, patients, samples, and treatments
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


### cBioNavigator

Assists with navigating the cBioPortal web interface:

- Describe what you want to see in plain English, and get a link to the most relevant cBioPortal page
- Guidance on navigating through studies and visualizations
- Support for exploring cBioPortal's web-based tools

**Example Queries:**
- [Navigate to the MSK-IMPACT study](https://chat.cbioportal.org/c/new?q=Navigate%20to%20the%20MSK-IMPACT%20study&submit=true&spec=cBioNavigator)
- [Show me an OncoPrint for BRAF and CDKN2A in melanoma](https://chat.cbioportal.org/c/new?q=Show%20me%20an%20OncoPrint%20for%20BRAF%20and%20CDKN2A%20in%20melanoma&submit=true&spec=cBioNavigator)
- [Compare low grade glioma by molecular subtype](https://chat.cbioportal.org/c/new?q=Compare%20low%20grade%20glioma%20by%20molecular%20subtype&submit=true&spec=cBioNavigator)
- [Show me patient view for IDH1 mutant cholangiocarcinoma](https://chat.cbioportal.org/c/new?q=Show%20me%20patient%20view%20for%20IDH1%20mutant%20cholangiocarcinoma&submit=true&spec=cBioNavigator)


## Tips for Using the Chat Interface

- **Choose the right agent**: Select the agent that best matches your need - data queries (cBioDBAgent) or web navigation (cBioNavigator)
- **Be specific**: Include study names or specific data types when possible
- **Ask follow-up questions**: The chat interface maintains context, so you can ask related questions in sequence
- **Explore different angles**: Try rephrasing questions or asking for different perspectives on the data
- **Learn about the database structure** (cBioDBAgent): You can ask the LLM to explain the database schema and available fields - this can help you formulate more effective questions

## Getting Started

1. Fill out this [form](https://docs.google.com/forms/d/e/1FAIpQLSfQ53xWgzZRu5qMINOqZCfK_8StG7bjbtJ7WsQM9fZpe1bq3A/viewform) to request access. We are onboarding new users in groups, so we appreciate your patience until we get in touch.
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

**cBioNavigator:**
- **Web Interface Tools**: Uses MCP tools to interact with the cBioPortal website

For more information about the MCP servers and how to build your own integrations, see the [Model Context Protocol documentation](mcp.md).
