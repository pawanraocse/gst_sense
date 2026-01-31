"""
AWS Lambda function for Cognito PreTokenGeneration trigger.

Simplified for Single-Tenant Lite Architecture.
Always assigns 'default' tenant ID to satisfy Gateway requirements.
"""

import logging
from typing import Any, Dict

# Configure logging
logger = logging.getLogger()
logger.setLevel(logging.INFO)

def lambda_handler(event: Dict[str, Any], context: Any) -> Dict[str, Any]:
    """
    Handle Cognito PreTokenGeneration trigger.
    Always injects default tenant context.
    """
    try:
        trigger_source = event.get('triggerSource', '')
        
        # Only process token generation events
        if 'TokenGeneration' not in trigger_source:
            return event
            
        # Build claims override - everyone is in the 'default' system tenant
        claims_to_override = {
            'custom:tenantId': 'default',
            'custom:tenantType': 'PERSONAL'
        }
        
        # Set the override in the response
        event['response'] = event.get('response', {})
        event['response']['claimsAndScopeOverrideDetails'] = {
            'idTokenGeneration': {
                'claimsToAddOrOverride': claims_to_override
            },
            'accessTokenGeneration': {
                'claimsToAddOrOverride': claims_to_override
            }
        }
        
        logger.info(f"Injected default tenant context for user {event.get('userName')}")
        
    except Exception as e:
        logger.error(f"Error in PreTokenGeneration: {str(e)}", exc_info=True)
        # Don't block auth on error, just log
    
    return event
