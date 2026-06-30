import React from 'react';

export default function ErrorMsg({ msg }) {
  if (!msg) return null;
  return (
    <div style={{ background: '#fee', color: '#c00', border: '1px solid #f99', borderRadius: 6, padding: '10px 16px', marginBottom: 12 }}>
      ⚠️ {msg}
    </div>
  );
}
