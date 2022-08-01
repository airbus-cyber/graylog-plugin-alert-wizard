/*
 * Copyright (C) 2018 Airbus CyberSecurity (SAS)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */

const FileSaver = {
  save(data, filename, mime, charset) {
    const link = document.createElement('a');

    const effectiveCharset = charset ? `;charset=${charset}` : '';
    const contentType = charset ? `${mime}${effectiveCharset}` : mime;

    // On modern browsers (Chrome and Firefox), use download property and a temporary link
    if (link.download !== undefined) {
      link.download = filename;
      link.href = `data:${contentType},${encodeURIComponent(data)}`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);

      return;
    }

    // On IE >= 10, use msSaveOrOpenBlob
    if (window.navigator && typeof window.navigator.msSaveOrOpenBlob === 'function') {
      const blob = new Blob([data], { type: contentType });
      window.navigator.msSaveOrOpenBlob(blob, filename);

      return;
    }

    try {
      // On Safari and other browsers, try to open the JSON as attachment
      location.href = `data:application/attachment${effectiveCharset},${encodeURIComponent(data)}`;
    } catch (e) {
      // If nothing else works, open the JSON as plain text in the browser
      location.href = `data:text/plain${effectiveCharset},${encodeURIComponent(data)}`;
    }
  },
};

export default FileSaver;