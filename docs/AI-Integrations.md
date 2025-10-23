# AI Integrations

cBioPortal is exploring the use of generative AI and large language models (LLMs) to make cancer genomics data more accessible and easier to explore. These AI-powered tools help users query and analyze the portal's extensive cancer datasets using natural language.

## Chat Interface

We've developed an AI-powered chat interface that allows users to ask questions about cBioPortal data in plain English. The chat interface is available at:

**[https://chat.cbioportal.org/](https://chat.cbioportal.org/)**

### What is the Chat Interface?

The cBioPortal chat interface uses Claude, an LLM developed by Anthropic, to help you explore cancer genomics data through conversational queries. The system translates natural language questions into database queries that are executed directly against cBioPortal's ClickHouse database. Instead of navigating through multiple pages or writing complex queries, you can simply ask questions about studies, patients, samples, and treatments in natural language.

### Key Features

- **Natural Language Queries**: Ask questions in plain English without needing to know the technical details of the database structure
- **Study Information**: Get quick answers about the number of studies, patients, and samples in cBioPortal
- **Sample Analysis**: Query specific details about samples, including sample types and patient demographics
- **Treatment Data**: Explore treatment information across different studies
- **Data Exploration**: Discover insights from the extensive cancer genomics datasets available in cBioPortal

### Example Queries

Here are some example questions you can ask the chat interface:

#### General Portal Information
- "How many studies are in cBioPortal?"

#### Study-Specific Queries
- "How many patients and samples are in the MSK-CHORD Study?"
- "How many primary samples are in the MSK-CHORD Study?"
- "What is treatment did most patients receive in the MSK-CHORD Study?"

#### Tips for Using the Chat Interface

- **Be specific**: Include study names or specific data types when possible
- **Ask follow-up questions**: The chat interface maintains context, so you can ask related questions in sequence
- **Explore different angles**: Try rephrasing questions or asking for different perspectives on the data
- **Learn about the database structure**: You can ask the LLM to explain the database schema and available fields - this can help you formulate more effective questions

### Getting Started

1. Visit [chat.cbioportal.org](https://chat.cbioportal.org/)
2. Type your question in the chat input
3. Review the AI-generated response
4. Ask follow-up questions to dive deeper into the data

### Feedback and Support

The AI chat interface is actively being developed and improved. Your feedback helps us make it better:

- **Use the thumbs up/down buttons**: Each chat response has thumbs up and thumbs down buttons. Please use them to rate the quality and accuracy of the responses - this feedback directly helps us improve the AI's answers.
- **Report issues or suggestions**: If you encounter any issues or have suggestions for improvement, please reach out through the [cBioPortal Google Group](https://groups.google.com/g/cbioportal).

---

## Future AI Integrations

We are continuously working on expanding AI capabilities within cBioPortal to make cancer genomics research more accessible and efficient. Stay tuned for updates on new AI-powered features.
