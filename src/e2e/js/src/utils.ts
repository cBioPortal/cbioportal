import axios, { AxiosResponse } from 'axios';
import { promises as fs } from 'fs';
import * as path from 'path';
import { AlterationEnrichment, EnrichmentType } from './types';
import { config } from './config';

export class TestUtils {

  /**
   * Calls the alteration enrichments API endpoint
   * @param testData - The request payload containing molecular profile case identifiers and filters
   * @param enrichmentType - The enrichment type (SAMPLE or PATIENT)
   * @returns Promise containing array of alteration enrichments
   */
  static async callEnrichmentEndpoint(
    testData: any,
    enrichmentType: EnrichmentType = EnrichmentType.SAMPLE
  ): Promise<AlterationEnrichment[]> {
    const url = `${config.serverUrl}/api/column-store/alteration-enrichments/fetch?enrichmentType=${enrichmentType}`;

    const response: AxiosResponse<AlterationEnrichment[]> = await axios.post(
      url,
      testData,
      {
        headers: {
          'Content-Type': 'application/json'
        }
      }
    );

    if (response.status !== 200) {
      throw new Error(`Expected status 200 but got ${response.status}`);
    }

    if (!response.data) {
      throw new Error('Response body is empty');
    }

    return response.data;
  }

  /**
   * Loads and parses a test data JSON file from the same directory as the calling test
   * @param filename - The name of the JSON file
   * @returns Promise containing the parsed JSON object
   */
  static async loadTestData(filename: string): Promise<any> {
    // Get the caller's file path from the stack trace
    const error = new Error();
    const stack = error.stack || '';
    const callerLine = stack.split('\n')[2] || '';

    // Extract file path from stack trace (works for both compiled and ts-node)
    const match = callerLine.match(/\((.+?):\d+:\d+\)/) || callerLine.match(/at (.+?):\d+:\d+/);

    if (!match || !match[1]) {
      throw new Error('Could not determine caller file path from stack trace');
    }

    const callerPath = match[1];
    const callerDir = path.dirname(callerPath);

    // Load the file from the same directory as the test
    const filePath = path.join(callerDir, filename);
    const fileContent = await fs.readFile(filePath, 'utf-8');
    return JSON.parse(fileContent);
  }
}