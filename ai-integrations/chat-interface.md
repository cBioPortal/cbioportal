# Chat Interface

We're developing an AI-powered chat interface that lets users explore cBioPortal data with natural language. We're looking for early test users to give us feedback and help guide future development. If that sounds like you, sign up [here](https://docs.google.com/forms/d/e/1FAIpQLSfQ53xWgzZRu5qMINOqZCfK_8StG7bjbtJ7WsQM9fZpe1bq3A/viewform).

> **Note:** This is a prototype and work in progress. Features and functionality are actively being developed and improved.

## What is the Chat Interface?

The cBioPortal chat interface (**cBioPortalChat**) is an experimental platform that lets you ask cBioPortal questions in plain English. It uses [Claude](https://www.anthropic.com/claude) (Anthropic's LLM, provided via Amazon Bedrock) and combines two capabilities in a single agent:

- **Database queries** — answers about studies, patients, samples, mutations, copy-number changes, clinical attributes, and treatments by querying cBioPortal's underlying database.
- **Web navigation** — generates direct links into cBioPortal views (study summaries, OncoPrint, patient view, group comparison) when you describe what you want to look at.

You don't need to pick which one you want — the agent decides which tools to call based on your question.

> **Earlier prototypes:** through 2026-Q2 the platform exposed two separate agents (cBioDBAgent and cBioNavigator) that the user had to choose between. These have been merged into a single cBioPortalChat agent. The combined agent has access to all of the tools the two earlier prototypes had.

## What you can ask

**Explore data** — find studies, datasets, and available molecular data:

- [Which cBioPortal studies include lung adenocarcinoma samples with mutation and copy-number data?](https://chat.cbioportal.org/c/new?q=Which%20cBioPortal%20studies%20include%20lung%20adenocarcinoma%20samples%20with%20mutation%20and%20copy-number%20data%3F&submit=true&spec=cBioPortalChat)
- [Which studies have RNA expression for renal cancer?](https://chat.cbioportal.org/c/new?q=Which%20studies%20have%20RNA%20expression%20for%20renal%20cancer%3F&submit=true&spec=cBioPortalChat)
- [How many studies have whole exome sequencing data?](https://chat.cbioportal.org/c/new?q=How%20many%20studies%20have%20whole%20exome%20sequencing%20data%3F&submit=true&spec=cBioPortalChat)
- [What TCGA data do you have?](https://chat.cbioportal.org/c/new?q=What%20TCGA%20data%20do%20you%20have%3F&submit=true&spec=cBioPortalChat)

**Navigate cBioPortal** — generate direct links to views:

- [Give me an OncoPrint for EGFR and KRAS in TCGA lung adenocarcinoma.](https://chat.cbioportal.org/c/new?q=Give%20me%20an%20OncoPrint%20for%20EGFR%20and%20KRAS%20in%20TCGA%20lung%20adenocarcinoma.&submit=true&spec=cBioPortalChat)
- [Can you create a cohort of metastatic prostate cancer with AR amplification?](https://chat.cbioportal.org/c/new?q=Can%20you%20create%20a%20cohort%20of%20metastatic%20prostate%20cancer%20with%20AR%20amplification%3F&submit=true&spec=cBioPortalChat)
- [Show me a KM plot of primary vs met prostate cancer in the MSK-IMPACT study.](https://chat.cbioportal.org/c/new?q=Show%20me%20a%20KM%20plot%20of%20primary%20vs%20met%20prostate%20cancer%20in%20the%20MSK-IMPACT%20study.&submit=true&spec=cBioPortalChat)

**Analyze data** — compare genes, cancer types, profiles, cohorts, and outcomes:

- [Show me TP53 mutation frequency across all cancer types.](https://chat.cbioportal.org/c/new?q=Show%20me%20TP53%20mutation%20frequency%20across%20all%20cancer%20types.&submit=true&spec=cBioPortalChat)
- [What are the most mutated genes in lung cancer?](https://chat.cbioportal.org/c/new?q=What%20are%20the%20most%20mutated%20genes%20in%20lung%20cancer%3F&submit=true&spec=cBioPortalChat)
- [Compare low grade glioma by molecular subtype.](https://chat.cbioportal.org/c/new?q=Compare%20low%20grade%20glioma%20by%20molecular%20subtype.&submit=true&spec=cBioPortalChat)

## Tips for Using the Chat Interface

- **Be specific** — include study names, gene symbols (UPPERCASE HUGO format), and data types when possible.
- **Ask follow-up questions** — the chat keeps conversation context, so you can refine in sequence.
- **Try rephrasing** — different angles often surface different views of the same data.
- **Ask about the schema** — you can ask the agent to explain available fields and tables to help frame more effective queries.
- **Statistical claims need external tools** — the agent can return summary data and generate links (including Kaplan–Meier plots), but it cannot compute statistical test results (p-values, hazard ratios, odds ratios). Use cBioPortal's Group Comparison tab, R, or Python to run the test.

## Getting Started

1. Fill out [this form](https://docs.google.com/forms/d/e/1FAIpQLSfQ53xWgzZRu5qMINOqZCfK_8StG7bjbtJ7WsQM9fZpe1bq3A/viewform) to request access. We onboard new users in groups — we appreciate your patience until we get in touch.
2. Type your question in the chat input.
3. Review the AI-generated response.
4. Ask follow-up questions to dive deeper.

## Feedback and Support

The chat interface is actively being developed and improved. Your feedback drives it:

- **Use the thumbs up/down buttons** — every chat response has 👍 / 👎 buttons. Please use them to rate quality and accuracy. This feedback directly improves the agent's answers.
- **Report issues or suggestions** — for anything beyond a quick rating, please reach out through the [cBioPortal Google Group](https://groups.google.com/g/cbioportal).

## Technical Details

### Architecture

- **User interface:** [LibreChat](https://github.com/danny-avila/LibreChat), an open-source chat front-end.
- **AI model:** [Claude](https://www.anthropic.com/claude), provided via Amazon Bedrock.
- **Tooling layer:** the agent calls [Model Context Protocol (MCP)](mcp.md) servers for its data and navigation capabilities:
  - A **database MCP** (built on cBioPortal's [ClickHouse](https://clickhouse.com/) database) for study, sample, mutation, and clinical-attribute queries.
  - A **navigator MCP** that converts intent into cBioPortal URLs.

A single unified agent has access to all of these tools and decides which to invoke for each turn.

For more information about the MCP servers and how to build your own integrations, see the [Model Context Protocol documentation](mcp.md).
