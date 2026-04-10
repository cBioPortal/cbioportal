# Accessibility Conformance Report
## WCAG Edition
(Based on VPAT&reg; Version 2.4)

**Name of Product/Version:** cBioPortal for Cancer Genomics (Latest)

**Report Date:** April 8, 2026

**Product Description:** An open-source platform for interactive exploration of multidimensional cancer genomics data sets.

**Contact Information:** [cbioportal@googlegroups.com](mailto:cbioportal@googlegroups.com)

**Notes:** This is an initial report based on a preliminary review of the core application features.

**Evaluation Methods Used:** Internal evaluation based on manual testing and automated accessibility auditing tools.

## Applicable Standards/Guidelines
This report covers the degree of conformance for the following accessibility standard/guidelines:

| Standard/Guideline | Included In Report |
| --- | --- |
| [Web Content Accessibility Guidelines 2.0](https://www.w3.org/TR/2008/REC-WCAG20-20081211/) | Level A (Yes) / Level AA (Yes) |
| [Web Content Accessibility Guidelines 2.1](https://www.w3.org/TR/WCAG21/) | Level A (Yes) / Level AA (Yes) |

## Terms
The terms used in the Conformance Level information are defined as follows:
*   **Supports:** The functionality of the product has at least one method that meets the criterion without known defects or meets with an equivalent facilitation.
*   **Partially Supports:** Some functionality of the product does not meet the criterion.
*   **Does Not Support:** The majority of product functionality does not meet the criterion.
*   **Not Applicable:** The criterion is not relevant to the product.
*   **Not Evaluated:** The product has not been evaluated against the criterion. This can be used only in WCAG 2.1 Level AAA.

## WCAG 2.x Report
*Note: When reporting on conformance with the WCAG 2.x Success Criteria, they are scoped for full pages, complete processes, and accessibility-supported ways of using technology as documented in the WCAG 2.x Conformance Requirements.*

### Table 1: Success Criteria, Level A
| Criteria | Conformance Level | Remarks and Explanations |
| --- | --- | --- |
| [1.1.1 Non-text Content](https://www.w3.org/WAI/WCAG21/Understanding/non-text-content.html) | Partially Supports | Most images have alternative text, but some complex data visualizations need further work. |
| [1.2.1 Audio-only and Video-only (Prerecorded)](https://www.w3.org/WAI/WCAG21/Understanding/audio-only-and-video-only-prerecorded.html) | Not Applicable | The platform does not currently use audio or video as primary content. |
| [1.3.1 Info and Relationships](https://www.w3.org/WAI/WCAG21/Understanding/info-and-relationships.html) | Partially Supports | Structure is generally maintained, but some complex tables and forms need better ARIA support. |
| [1.4.1 Use of Color](https://www.w3.org/WAI/WCAG21/Understanding/use-of-color.html) | Partially Supports | Color is used to convey information in many charts; work is ongoing to provide alternative patterns and shapes. |
| [2.1.1 Keyboard](https://www.w3.org/WAI/WCAG21/Understanding/keyboard.html) | Partially Supports | Most features are keyboard accessible, but some interactive charts have limitations. |
| [2.4.1 Bypass Blocks](https://www.w3.org/WAI/WCAG21/Understanding/bypass-blocks.html) | Supports | Skip to main content links are provided. |

### Table 2: Success Criteria, Level AA
| Criteria | Conformance Level | Remarks and Explanations |
| --- | --- | --- |
| [1.4.3 Contrast (Minimum)](https://www.w3.org/WAI/WCAG21/Understanding/contrast-minimum.html) | Partially Supports | Most text elements meet contrast requirements, but some UI components and charts need refinement. |
| [2.4.6 Headings and Labels](https://www.w3.org/WAI/WCAG21/Understanding/headings-and-labels.html) | Supports | Descriptive headings and labels are widely used. |
| [3.3.3 Error Suggestion](https://www.w3.org/WAI/WCAG21/Understanding/error-suggestion.html) | Supports | Input errors are identified and suggestions are provided where possible. |

---
*VPAT&reg; is a registered service mark of the Information Technology Industry Council (ITI)*
