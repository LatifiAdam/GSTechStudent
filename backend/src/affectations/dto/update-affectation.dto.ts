import { PartialType } from '@nestjs/mapped-types';
import { CreateAffectationDto } from './create-affectation.dto';

export class UpdateAffectationDto extends PartialType(CreateAffectationDto) {}
